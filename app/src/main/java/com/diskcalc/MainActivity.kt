package com.diskcalc

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.diskcalc.core.update.UpdateService
import com.diskcalc.ui.DiskPartitionCalculatorApp

/** Single activity hosting the whole Compose UI. */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { DiskPartitionCalculatorApp() }
    }

    @Deprecated("Result of the Google Play in-app update flow.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UpdateService.onActivityResult(requestCode, resultCode)
    }
}
