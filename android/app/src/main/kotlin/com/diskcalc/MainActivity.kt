package com.diskcalc

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Process
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            "com.diskcalc/restart"
        ).setMethodCallHandler { call, result ->
            if (call.method == "restartApp") {
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                if (intent != null) {
                    intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                    )
                    result.success(null)
                    startActivity(intent)
                    finishAffinity()
                    Handler(Looper.getMainLooper()).postDelayed({
                        Process.killProcess(Process.myPid())
                    }, 500)
                } else {
                    result.error("NO_INTENT", "Could not get launch intent", null)
                }
            } else {
                result.notImplemented()
            }
        }
    }
}
