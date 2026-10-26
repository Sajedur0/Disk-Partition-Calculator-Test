package com.diskcalc.update

import android.app.Activity
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

/**
 * Google Play In-App Updates — **FLEXIBLE** flow with Play's own native UI.
 *
 * - User accepts → Play downloads in the background → the update is installed **automatically**
 *   as soon as the download finishes (`completeUpdate()` restarts the app). No second prompt.
 * - User cancels → nothing else happens; the calculator keeps running normally and the Play UI
 *   is not shown again during this process.
 *
 * The download, permission prompt and progress UI all belong to Google Play; the app only starts
 * the flow and observes the install state. Everything is defensive: sideloaded builds / builds
 * without Play Services simply get no update prompt instead of a crash, so the app keeps working
 * fully offline. No calculation, history or preference data is ever transmitted.
 */
class UpdateService(private val activity: Activity) {

    private val manager: AppUpdateManager? = runCatching {
        AppUpdateManagerFactory.create(activity.applicationContext)
    }.getOrNull()

    private var listenerRegistered = false

    /** After a cancel (or an unavailable Play UI) do not offer the flow again in this process. */
    private var userDeclined = false

    /** The DOWNLOADED event can arrive more than once — install only once. */
    private var completingUpdate = false

    private val installStateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            completeUpdate()
        }
    }

    /** Called once from `MainActivity.onCreate`. Asks Play whether a flexible update is available. */
    fun checkForUpdate() {
        val updateManager = manager ?: return
        if (userDeclined) return
        runCatching {
            updateManager.appUpdateInfo
                .addOnSuccessListener { info ->
                    val available = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    val inProgress = info.updateAvailability() ==
                        UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                    if ((available || inProgress) && info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                        startFlexibleUpdate(info)
                    }
                }
                .addOnFailureListener {
                    // Offline, no Play Store or not installed from Play — nothing to do.
                    userDeclined = true
                }
        }
    }

    private fun startFlexibleUpdate(info: AppUpdateInfo) {
        val updateManager = manager ?: return
        runCatching {
            val options = AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
            // Play renders the native confirmation/download UI; we don't show a custom flow.
            updateManager.startUpdateFlow(info, activity, options)
                .addOnSuccessListener { result ->
                    if (result == Activity.RESULT_CANCELED) {
                        // User pressed Cancel — no retry, no restart, app keeps working as-is.
                        userDeclined = true
                    }
                }
                .addOnFailureListener {
                    // Play UI unavailable or backed out — keep the app running normally.
                    userDeclined = true
                }
        }.onFailure {
            // Some Play Store versions throw instead of failing the Task.
            userDeclined = true
        }
    }

    /** Registers the listener and finishes a download that completed while the app was away. */
    fun onResume() {
        val updateManager = manager ?: return
        if (!listenerRegistered) {
            runCatching {
                updateManager.registerListener(installStateListener)
                listenerRegistered = true
            }
        }
        runCatching {
            updateManager.appUpdateInfo.addOnSuccessListener { info ->
                if (info.installStatus() == InstallStatus.DOWNLOADED) {
                    completeUpdate()
                }
            }
        }
    }

    fun onPause() {
        if (listenerRegistered) {
            runCatching { manager?.unregisterListener(installStateListener) }
            listenerRegistered = false
        }
    }

    /** Download finished — install immediately (Play restarts the app). No extra confirmation. */
    private fun completeUpdate() {
        if (completingUpdate) return
        completingUpdate = true
        val outcome = runCatching { manager?.completeUpdate() }
        // Allow a later onResume to retry if Play rejected this particular call.
        if (outcome.isFailure) completingUpdate = false
    }
}
