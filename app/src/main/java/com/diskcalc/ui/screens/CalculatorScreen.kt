package com.diskcalc.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diskcalc.BuildConfig
import com.diskcalc.R
import com.diskcalc.core.model.ThemeMode
import com.diskcalc.core.util.AppActions
import com.diskcalc.ui.components.HistorySheet
import com.diskcalc.ui.components.QuitDialog
import com.diskcalc.ui.components.ResultDialog
import com.diskcalc.ui.text.AppStrings
import com.diskcalc.ui.theme.Motion
import com.diskcalc.ui.viewmodel.CalcViewModel
import com.diskcalc.ui.viewmodel.HistoryViewModel
import com.diskcalc.ui.viewmodel.SplitViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** The three destinations of the home screen. */
private enum class HomeTab(val label: String, val iconRes: Int) {
    CALC(AppStrings.Tabs.CALC, R.drawable.ic_calculate),
    SPLIT(AppStrings.Tabs.SPLIT, R.drawable.ic_call_split),
    SETTINGS(AppStrings.Tabs.SETTINGS, R.drawable.ic_settings),
}

/**
 * Home screen: title bar with the history action, bottom navigation between the Calc, Split and
 * Settings tabs, plus the result, history and quit dialogs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    calcViewModel: CalcViewModel,
    splitViewModel: SplitViewModel,
    historyViewModel: HistoryViewModel,
    themeMode: ThemeMode,
    onToggleTheme: (Offset) -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenAbout: () -> Unit,
    onConfirmQuit: () -> Unit,
) {
    val calcState by calcViewModel.state.collectAsStateWithLifecycle()
    val splitState by splitViewModel.state.collectAsStateWithLifecycle()
    val history by historyViewModel.history.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedTab by rememberSaveable { mutableIntStateOf(HomeTab.CALC.ordinal) }
    var showHistory by rememberSaveable { mutableStateOf(false) }
    var showQuitDialog by rememberSaveable { mutableStateOf(false) }

    ShowSnackbarFor(calcState.message) { message ->
        snackbarHostState.showSnackbar(message)
        calcViewModel.onMessageShown()
    }

    ShowSnackbarFor(splitState.message) { message ->
        snackbarHostState.showSnackbar(message)
        splitViewModel.onMessageShown()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppStrings.APP_NAME,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                actions = {
                    if (selectedTab != HomeTab.SETTINGS.ordinal) {
                        IconButton(onClick = { showHistory = true }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_history),
                                contentDescription = AppStrings.Store.HISTORY_TOOLTIP,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                HomeTab.entries.forEach { tab ->
                    val selected = selectedTab == tab.ordinal
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedTab = tab.ordinal
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(tab.iconRes),
                                contentDescription = null,
                            )
                        },
                        label = { Text(text = tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        },
    ) { scaffoldPadding ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (
                    slideInHorizontally { width -> direction * width / 8 } +
                        fadeIn(animationSpec = tween(Motion.BASE))
                    ) togetherWith
                    (
                        slideOutHorizontally { width -> -direction * width / 8 } +
                            fadeOut(animationSpec = tween(Motion.FAST))
                        )
            },
            label = "home tabs",
        ) { tabIndex ->
            when (HomeTab.entries[tabIndex]) {
                HomeTab.CALC -> CalcTab(
                    state = calcState,
                    onSizeChange = calcViewModel::onSizeInputChange,
                    onPresetSelected = calcViewModel::onPresetSelected,
                    onFilesystemChange = calcViewModel::onFilesystemChange,
                    onSimulationChange = calcViewModel::onSimulationChange,
                    onCalculate = calcViewModel::calculate,
                    contentPadding = scaffoldPadding,
                )

                HomeTab.SPLIT -> SplitTab(
                    state = splitState,
                    onTotalSizeChange = splitViewModel::onTotalSizeChange,
                    onPartitionCountIncrement = splitViewModel::incrementPartitionCount,
                    onPartitionCountDecrement = splitViewModel::decrementPartitionCount,
                    onFilesystemChange = splitViewModel::onFilesystemChange,
                    onSplit = splitViewModel::split,
                    contentPadding = scaffoldPadding,
                )

                HomeTab.SETTINGS -> SettingsTab(
                    isDarkTheme = themeMode.isDark,
                    versionName = BuildConfig.VERSION_NAME,
                    onToggleTheme = onToggleTheme,
                    onShareApp = { AppActions.shareApp(context) },
                    onRateUs = {
                        if (!AppActions.rateApp(context)) {
                            scope.showSnackbar(
                                snackbarHostState,
                                AppStrings.Store.COULD_NOT_OPEN_STORE,
                            )
                        }
                    },
                    onPrivacyPolicy = onOpenPrivacyPolicy,
                    onAbout = onOpenAbout,
                    contentPadding = scaffoldPadding,
                )
            }
        }
    }

    calcState.result?.let { result ->
        ResultDialog(
            content = result,
            onCopy = { value ->
                context.copyRequiredMb(value)
                scope.showSnackbar(snackbarHostState, AppStrings.Result.COPIED)
            },
            onDismiss = calcViewModel::onResultDismissed,
        )
    }

    splitState.result?.let { result ->
        ResultDialog(
            content = result,
            onCopy = { value ->
                context.copyRequiredMb(value)
                scope.showSnackbar(snackbarHostState, AppStrings.Result.COPIED)
            },
            onDismiss = splitViewModel::onResultDismissed,
        )
    }

    if (showHistory) {
        HistorySheet(
            history = history,
            onClearAll = {
                historyViewModel.clearHistory()
                showHistory = false
                scope.showSnackbar(snackbarHostState, AppStrings.History.CLEARED)
            },
            onDismiss = { showHistory = false },
        )
    }

    if (showQuitDialog) {
        QuitDialog(
            onDismiss = { showQuitDialog = false },
            onConfirm = {
                showQuitDialog = false
                onConfirmQuit()
            },
        )
    }
}

/** Shows [message] as a snack bar and then clears it from the view model state. */
@Composable
private fun ShowSnackbarFor(message: String?, onShown: suspend (String) -> Unit) {
    LaunchedEffect(message) {
        message?.let { onShown(it) }
    }
}

private fun Context.copyRequiredMb(value: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(AppStrings.APP_NAME, value))
}

private fun CoroutineScope.showSnackbar(hostState: SnackbarHostState, message: String) {
    launch { hostState.showSnackbar(message) }
}
