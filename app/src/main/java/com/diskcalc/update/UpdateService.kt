package com.diskcalc.update

import android.app.Activity
import android.widget.Toast
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

/**
 * Thin wrapper around Play In-App Updates.
 *
 * Everything is defensive: builds that do not come from Play (local debug installs, sideloads)
 * simply get no update prompt instead of a crash, and the whole feature is optional at runtime.
 */
class UpdateService(private val activity: Activity) {

    private val manager: AppUpdateManager? = runCatching {
        AppUpdateManagerFactory.create(activity)
    }.getOrNull()

    private val installStateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            completeUpdate()
        }
    }

    /** Asks Play whether a newer version exists and starts a flexible download when allowed. */
    fun checkForUpdate() {
        val updateManager = manager ?: return
        runCatching {
            updateManager.appUpdateInfo
                .addOnSuccessListener { info ->
                    val available = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    val inProgress = info.updateAvailability() ==
                        UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                    val typeAllowed = info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                    if ((available || inProgress) && typeAllowed) {
                        startFlexibleUpdate(info)
                    }
                }
                .addOnFailureListener { /* offline or not installed from Play — nothing to do */ }
        }
    }

    private fun startFlexibleUpdate(info: com.google.android.play.core.appupdate.AppUpdateInfo) {
        runCatching {
            val options = AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
            manager?.startUpdateFlow(info, activity, options)
        }.onFailure {
            // Cancelled or unavailable: keep the app running on the current version.
        }
    }

    /** Registers the install-state listener and finishes a download that completed while away. */
    fun onResume() {
        val updateManager = manager ?: return
        runCatching {
            updateManager.registerListener(installStateListener)
            updateManager.appUpdateInfo.addOnSuccessListener { info ->
                if (info.installStatus() == InstallStatus.DOWNLOADED) {
                    completeUpdate()
                }
            }
        }
    }

    fun onPause() {
        runCatching { manager?.unregisterListener(installStateListener) }
    }

    private fun completeUpdate() {
        runCatching {
            Toast.makeText(activity, "Update ready — restarting", Toast.LENGTH_SHORT).show()
            manager?.completeUpdate()
        }
    }
}
