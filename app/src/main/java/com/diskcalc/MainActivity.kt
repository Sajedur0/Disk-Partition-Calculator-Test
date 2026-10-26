package com.diskcalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diskcalc.ui.CalculatorScreen
import com.diskcalc.ui.CalculatorViewModel

/**
 * Single-activity host. Edge-to-edge is enabled so the solid backdrop runs under the
 * system bars. The app has no Service, BroadcastReceiver, WorkManager or network call
 * (`docs/04_TRD.md §2/§4`).
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as DiskCalcApplication).container

        setContent {
            val viewModel: CalculatorViewModel = viewModel(
                factory = CalculatorViewModel.factory(container)
            )
            CalculatorScreen(viewModel = viewModel)
        }
    }
}
