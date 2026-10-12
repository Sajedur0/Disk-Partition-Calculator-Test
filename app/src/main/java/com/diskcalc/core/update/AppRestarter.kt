package com.diskcalc.core.update

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.util.Log

/**
 * Restarts the app in a fresh task — used after a flexible in-app update finishes installing,
 * mirroring the restart behaviour of the original app.
 */
internal object AppRestarter {

    private const val TAG = "AppRestarter"
    private const val KILL_DELAY_MS = 500L

    fun restart(context: Context) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (launchIntent == null) {
            Log.w(TAG, "Could not resolve the launch intent; skipping restart")
            return
        }

        launchIntent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP,
        )

        context.startActivity(launchIntent)
        (context as? Activity)?.finishAffinity()
        Handler(Looper.getMainLooper()).postDelayed(
            { Process.killProcess(Process.myPid()) },
            KILL_DELAY_MS,
        )
    }
}
