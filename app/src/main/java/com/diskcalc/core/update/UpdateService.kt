package com.diskcalc.core.update

import android.app.Activity
import android.content.IntentSender
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.InstallStatus
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

/**
 * Google Play in-app update orchestration, ported from the original app:
 *
 * * checks shortly after launch and whenever the app is resumed,
 * * hands the update over to Google Play's own (native) update UI, launched through the
 *   Activity Result API — a flexible download by default, an immediate update once the
 *   installed version is five days stale,
 * * never asks for the same flexible download twice: while Play is downloading or installing,
 *   the install-state listener simply waits for it,
 * * remembers a dismissal for the rest of the session,
 * * completes a downloaded flexible update and restarts the app (with a 30s safety net).
 */
object UpdateService {

    private const val TAG = "UpdateService"
    private const val CHECK_TIMEOUT_MS = 10_000L
    private const val FALLBACK_RESTART_MS = 30_000L
    private const val RESTART_DELAY_MS = 2_000L

    /** Number of stale days after which a mandatory update is offered. */
    private const val STALENESS_DAYS_FOR_IMMEDIATE = 5

    private val mainHandler = Handler(Looper.getMainLooper())

    private var appUpdateManager: AppUpdateManager? = null
    private var installStateListener: InstallStateUpdatedListener? = null
    private var fallbackRestart: Runnable? = null
    private var timeoutReset: Runnable? = null

    private var isChecking = false
    private var userDismissedInSession = false

    /**
     * Starts an update check. When Play reports an update, Google Play's native update UI is
     * opened through [launcher]. [isResume] checks are skipped once the user dismissed a flow.
     */
    fun checkForUpdate(
        activity: Activity,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        isResume: Boolean = false,
    ) {
        if (isChecking) return
        if (isResume && userDismissedInSession) return

        isChecking = true
        val manager = appUpdateManager ?: AppUpdateManagerFactory.create(activity).also {
            appUpdateManager = it
        }

        // Play Core normally answers quickly; the timer makes sure a silent failure cannot
        // block every following check for the rest of the session.
        scheduleTimeoutReset()

        manager.appUpdateInfo
            .addOnSuccessListener { info ->
                cancelTimeoutReset()
                isChecking = false
                processUpdateInfo(activity, manager, launcher, info)
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "Update check failed", error)
                cancelTimeoutReset()
                cancelListener()
                isChecking = false
            }
    }

    /** Handles the result of Google Play's native update UI. */
    fun onUpdateFlowResult(resultCode: Int) {
        if (resultCode != Activity.RESULT_OK) {
            userDismissedInSession = true
            cancelFallbackRestart()
            cancelListener()
        }
    }

    /** Releases listeners; called when the hosting activity goes away. */
    fun dispose() {
        cancelListener()
        cancelFallbackRestart()
        cancelTimeoutReset()
        isChecking = false
    }

    private fun processUpdateInfo(
        activity: Activity,
        manager: AppUpdateManager,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        info: AppUpdateInfo,
    ) {
        if (info.installStatus == InstallStatus.DOWNLOADED) {
            completeFlexibleUpdate(activity, manager)
            return
        }

        if (info.updateAvailability ==
            UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
        ) {
            if (userDismissedInSession) return
            listenForFlexibleUpdate(activity, manager)
            startUpdateFlow(manager, launcher, info, AppUpdateType.FLEXIBLE)
            return
        }

        if (info.updateAvailability != UpdateAvailability.UPDATE_AVAILABLE) return

        val staleness = info.clientVersionStalenessDays ?: 0
        if (info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) &&
            staleness >= STALENESS_DAYS_FOR_IMMEDIATE
        ) {
            startUpdateFlow(manager, launcher, info, AppUpdateType.IMMEDIATE)
            return
        }

        if (!info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) return

        // A flexible update that is already downloading or installing needs no second consent
        // dialog, so the listener takes over without opening the flow again. It is registered
        // without the fallback restart: a slow download must not restart the app midway.
        if (info.installStatus == InstallStatus.DOWNLOADING ||
            info.installStatus == InstallStatus.INSTALLING
        ) {
            listenForFlexibleUpdate(activity, manager, scheduleFallback = false)
            return
        }

        listenForFlexibleUpdate(activity, manager)
        startUpdateFlow(manager, launcher, info, AppUpdateType.FLEXIBLE)
    }

    /** Opens Google Play's native update UI for [updateType]. */
    private fun startUpdateFlow(
        manager: AppUpdateManager,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        info: AppUpdateInfo,
        updateType: Int,
    ) {
        val options = AppUpdateOptions.newBuilder(updateType).build()
        try {
            manager.startUpdateFlowForResult(info, launcher, options)
        } catch (error: IntentSender.SendIntentException) {
            Log.w(TAG, "Could not start the update flow", error)
            cancelFallbackRestart()
            cancelListener()
        }
    }

    private fun listenForFlexibleUpdate(
        activity: Activity,
        manager: AppUpdateManager,
        scheduleFallback: Boolean = true,
    ) {
        cancelListener()
        cancelFallbackRestart()

        val listener = InstallStateUpdatedListener { state: InstallState ->
            when (state.installStatus) {
                InstallStatus.DOWNLOADED -> {
                    cancelListener()
                    completeFlexibleUpdate(activity, manager)
                }

                InstallStatus.INSTALLED -> {
                    cancelFallbackRestart()
                    AppRestarter.restart(activity)
                    cancelListener()
                }

                InstallStatus.FAILED -> {
                    Log.w(
                        TAG,
                        "Flexible update failed (code ${state.installErrorCode})",
                    )
                    cancelFallbackRestart()
                    cancelListener()
                }

                InstallStatus.CANCELED -> {
                    cancelFallbackRestart()
                    cancelListener()
                }

                else -> Unit
            }
        }

        installStateListener = listener
        manager.registerListener(listener)
        if (scheduleFallback) scheduleFallbackRestart(activity)
    }

    private fun completeFlexibleUpdate(activity: Activity, manager: AppUpdateManager) {
        cancelFallbackRestart()
        manager.completeUpdate()
            .addOnSuccessListener {
                mainHandler.postDelayed(
                    { AppRestarter.restart(activity) },
                    RESTART_DELAY_MS,
                )
            }
            .addOnFailureListener { error -> Log.w(TAG, "Completing the update failed", error) }
    }

    private fun scheduleFallbackRestart(activity: Activity) {
        val runnable = Runnable {
            Log.w(TAG, "Flexible update listener timed out; restarting the app")
            cancelListener()
            AppRestarter.restart(activity)
        }
        fallbackRestart = runnable
        mainHandler.postDelayed(runnable, FALLBACK_RESTART_MS)
    }

    private fun cancelFallbackRestart() {
        fallbackRestart?.let { mainHandler.removeCallbacks(it) }
        fallbackRestart = null
    }

    private fun scheduleTimeoutReset() {
        cancelTimeoutReset()
        val runnable = Runnable {
            Log.w(TAG, "Update check timed out")
            isChecking = false
        }
        timeoutReset = runnable
        mainHandler.postDelayed(runnable, CHECK_TIMEOUT_MS)
    }

    private fun cancelTimeoutReset() {
        timeoutReset?.let { mainHandler.removeCallbacks(it) }
        timeoutReset = null
    }

    private fun cancelListener() {
        installStateListener?.let { listener ->
            appUpdateManager?.unregisterListener(listener)
        }
        installStateListener = null
    }
}
