package com.diskcalc

import android.app.Application
import com.diskcalc.data.AppContainer

/** Owns the single [AppContainer] instance for the process. */
class DiskCalcApplication : Application() {

    val container: AppContainer by lazy { AppContainer(this) }
}
