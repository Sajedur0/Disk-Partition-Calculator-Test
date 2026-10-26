package com.diskcalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diskcalc.ui.CalculatorScreen
import com.diskcalc.ui.CalculatorViewModel
import com.diskcalc.update.UpdateService

/**
 * Single-activity host. Edge-to-edge is enabled so the solid backdrop runs under the
 * system bars. The only network-aware component is [UpdateService] (Google Play flexible
 * in-app updates); calculations, history and settings stay entirely on the device.
 */
class MainActivity : ComponentActivity() {

    private var updateService: UpdateService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        updateService = UpdateService(this)
        val container = (application as DiskCalcApplication).container

        setContent {
            val viewModel: CalculatorViewModel = viewModel(
                factory = CalculatorViewModel.factory(container)
            )
            CalculatorScreen(viewModel = viewModel)
        }

        updateService?.checkForUpdate()
    }

    override fun onResume() {
        super.onResume()
        updateService?.onResume()
    }

    override fun onPause() {
        updateService?.onPause()
        super.onPause()
    }
}
