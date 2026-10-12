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
 * Single-activity host. Edge-to-edge is enabled so the liquid backdrop can bleed under the
 * system bars, which is what makes the glass read as one continuous sheet.
 */
class MainActivity : ComponentActivity() {

    private lateinit var updateService: UpdateService

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        updateService = UpdateService(this)
        val container = (application as DiskCalcApplication).container

        setContent {
            val viewModel: CalculatorViewModel = viewModel(
                factory = CalculatorViewModel.factory(container)
            )
            CalculatorScreen(viewModel = viewModel)
        }

        updateService.checkForUpdate()
    }

    override fun onResume() {
        super.onResume()
        updateService.onResume()
    }

    override fun onPause() {
        updateService.onPause()
        super.onPause()
    }
}
