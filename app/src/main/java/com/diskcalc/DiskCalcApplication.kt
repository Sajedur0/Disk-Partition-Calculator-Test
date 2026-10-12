package com.diskcalc

import android.app.Application
import com.diskcalc.di.AppContainer

/** Application entry point; owns the process-wide dependency container. */
class DiskCalcApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
