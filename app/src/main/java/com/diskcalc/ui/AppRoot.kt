package com.diskcalc.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diskcalc.core.update.UpdateService
import com.diskcalc.ui.screens.AboutScreen
import com.diskcalc.ui.screens.CalculatorScreen
import com.diskcalc.ui.screens.PrivacyPolicyScreen
import com.diskcalc.ui.theme.ThemeRevealHost
import com.diskcalc.ui.theme.rememberThemeRevealState
import com.diskcalc.ui.viewmodel.AppViewModelProvider
import com.diskcalc.ui.viewmodel.CalcViewModel
import com.diskcalc.ui.viewmodel.HistoryViewModel
import com.diskcalc.ui.viewmodel.MainViewModel
import com.diskcalc.ui.viewmodel.SplitViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class AppDestination { HOME, ABOUT, PRIVACY }

/** Delay before the first update check, so the initial frame is never blocked. */
private const val FIRST_UPDATE_CHECK_DELAY_MS = 2_000L

/** Minimum gap between two resume-triggered update checks. */
private const val RESUME_UPDATE_CHECK_GAP_MS = 60_000L

/**
 * Application root: theme (with the circular reveal), in-app update checks and the simple
 * home / about / privacy navigation.
 */
@Composable
fun DiskPartitionCalculatorApp(
    mainViewModel: MainViewModel = viewModel(factory = AppViewModelProvider.Factory),
    calcViewModel: CalcViewModel = viewModel(factory = AppViewModelProvider.Factory),
    splitViewModel: SplitViewModel = viewModel(factory = AppViewModelProvider.Factory),
    historyViewModel: HistoryViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()
    val darkTheme = themeMode.isDark

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val revealState = rememberThemeRevealState()
    val scope = rememberCoroutineScope()

    var hostSize by remember { mutableStateOf(Size.Zero) }
    var destinationName by rememberSaveable { mutableStateOf(AppDestination.HOME.name) }
    val destination = AppDestination.valueOf(destinationName)
    var lastUpdateCheckAt by remember { mutableLongStateOf(0L) }

    // Google Play's native update UI (consent dialog / full-screen install screen) is launched
    // through the Activity Result API, which is the documented entry point for in-app updates.
    val updateFlowLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        UpdateService.onUpdateFlowResult(result.resultCode)
    }

    // First update check, after the UI is up and running.
    LaunchedEffect(activity) {
        if (activity == null) return@LaunchedEffect
        delay(FIRST_UPDATE_CHECK_DELAY_MS)
        lastUpdateCheckAt = SystemClock.elapsedRealtime()
        UpdateService.checkForUpdate(activity, updateFlowLauncher, isResume = false)
    }

    // Further checks whenever the app comes back to the foreground.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, activity) {
        val observer = LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_RESUME || activity == null) {
                return@LifecycleEventObserver
            }

            val now = SystemClock.elapsedRealtime()
            if (lastUpdateCheckAt != 0L && now - lastUpdateCheckAt >= RESUME_UPDATE_CHECK_GAP_MS) {
                lastUpdateCheckAt = now
                UpdateService.checkForUpdate(activity, updateFlowLauncher, isResume = true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(Unit) {
        onDispose { UpdateService.dispose() }
    }

    BackHandler(enabled = destination != AppDestination.HOME) {
        destinationName = AppDestination.HOME.name
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates -> hostSize = coordinates.size.toSize() },
    ) {
        ThemeRevealHost(state = revealState, darkTheme = darkTheme) {
            AnimatedContent(
                targetState = destination,
                transitionSpec = {
                    val forward = targetState.ordinal > initialState.ordinal
                    val direction = if (forward) 1 else -1
                    (
                        slideInHorizontally { width -> direction * width / 5 } +
                            fadeIn(animationSpec = tween(280))
                        ) togetherWith
                        (
                            slideOutHorizontally { width -> -direction * width / 5 } +
                                fadeOut(animationSpec = tween(160))
                            )
                },
                label = "destination",
            ) { screen ->
                when (screen) {
                    AppDestination.HOME -> CalculatorScreen(
                        calcViewModel = calcViewModel,
                        splitViewModel = splitViewModel,
                        historyViewModel = historyViewModel,
                        themeMode = themeMode,
                        onToggleTheme = { origin ->
                            val fromDark = darkTheme
                            scope.launch {
                                revealState.revealFrom(
                                    tapPosition = origin,
                                    hostSize = hostSize,
                                    fromDarkTheme = fromDark,
                                    switchTheme = { mainViewModel.toggleTheme() },
                                )
                            }
                        },
                        onOpenPrivacyPolicy = {
                            destinationName = AppDestination.PRIVACY.name
                        },
                        onOpenAbout = { destinationName = AppDestination.ABOUT.name },
                        onConfirmQuit = { activity?.finish() },
                    )

                    AppDestination.ABOUT -> AboutScreen(
                        onBack = { destinationName = AppDestination.HOME.name },
                    )

                    AppDestination.PRIVACY -> PrivacyPolicyScreen(
                        onBack = { destinationName = AppDestination.HOME.name },
                    )
                }
            }
        }
    }
}

/** Finds the hosting activity of a composition context. */
internal fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
