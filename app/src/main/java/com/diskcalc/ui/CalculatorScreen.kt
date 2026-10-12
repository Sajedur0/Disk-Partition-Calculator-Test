package com.diskcalc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diskcalc.domain.ThemeMode
import com.diskcalc.ui.components.GlassIconButton
import com.diskcalc.ui.components.GlassTabBar
import com.diskcalc.ui.components.GlassTabItem
import com.diskcalc.ui.components.LiquidGlassSurface
import com.diskcalc.ui.theme.DiskCalcTheme
import com.diskcalc.ui.theme.LiquidBackdropHost
import com.diskcalc.ui.theme.LocalGlassTokens
import com.diskcalc.ui.theme.LocalLiquidPalette
import kotlinx.coroutines.launch

/**
 * Host of the three tabs. Owns the theme, solid backdrop and dialogs.
 * All business state lives in [CalculatorViewModel].
 */
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val calc by viewModel.calc.collectAsStateWithLifecycle()
    val split by viewModel.split.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showHistory by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val tabs = remember {
        listOf(
            GlassTabItem(title = "Calc", icon = Icons.Rounded.Calculate),
            GlassTabItem(title = "Split", icon = Icons.Rounded.Dashboard),
            GlassTabItem(title = "Settings", icon = Icons.Rounded.Settings)
        )
    }

    DiskCalcTheme(themeMode = settings.themeMode) {
        LiquidBackdropHost(modifier = modifier) {
            val palette = LocalLiquidPalette.current
            val tokens = LocalGlassTokens.current

            Box(
                Modifier
                    .fillMaxSize()
                    .background(palette.backdropBase)
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(horizontal = tokens.screenPadding)
                ) {
                    AppHeader(
                        isDark = palette.isDark,
                        onToggleTheme = {
                            viewModel.onThemeChange(
                                if (palette.isDark) ThemeMode.LIGHT else ThemeMode.DARK
                            )
                        },
                        onHistoryClick = { showHistory = true },
                        historyCount = history.size
                    )
                    Spacer(Modifier.height(tokens.spaceMd))

                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        when (selectedTabIndex) {
                            0 -> CalcTab(
                                state = calc,
                                onVisibleGbChange = viewModel::onVisibleGbChange,
                                onPresetSelected = viewModel::applyPreset,
                                onFilesystemChange = viewModel::onCalcFilesystemChange,
                                onSetupToggle = viewModel::onCalcSetupToggle,
                                onProfileChange = viewModel::onSetupProfileChange,
                                onCustomOverheadChange = viewModel::onCustomOverheadChange,
                                onCalculate = viewModel::calculate
                            )

                            1 -> SplitTab(
                                state = split,
                                onTotalMbChange = viewModel::onTotalMbChange,
                                onPartitionCountChange = viewModel::onPartitionCountChange,
                                onFilesystemChange = viewModel::onSplitFilesystemChange,
                                onSetupToggle = viewModel::onSplitSetupToggle,
                                onSplit = { viewModel.split() }
                            )

                            else -> SettingsTab(
                                settings = settings,
                                historySize = history.size,
                                onThemeChange = viewModel::onThemeChange,
                                onDefaultFilesystemChange = viewModel::onDefaultFilesystemChange,
                                onDefaultSetupChange = viewModel::onDefaultSetupChange,
                                onDefaultProfileChange = viewModel::onDefaultProfileChange,
                                onClearHistory = viewModel::onClearHistory,
                                onShowHistory = { showHistory = true }
                            )
                        }
                    }

                    Spacer(Modifier.height(tokens.spaceSm))
                    GlassTabBar(
                        items = tabs,
                        selectedIndex = selectedTabIndex,
                        onSelect = { selectedTabIndex = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(tokens.spaceSm))
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(bottom = 92.dp, start = 24.dp, end = 24.dp)
                ) { data ->
                    LiquidGlassSurface(
                        shape = RoundedCornerShape(percent = 50),
                        tint = palette.glassTint,
                        elevation = tokens.elevationSmall
                    ) {
                        Text(
                            text = data.visuals.message,
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.contentPrimary,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                        )
                    }
                }
            }

            // ------------------------------------------------------------ dialogs
            calc.result?.let { result ->
                CalcResultDialog(
                    result = result,
                    onDismiss = viewModel::dismissCalcResult,
                    onCopied = {
                        scope.launch { snackbarHostState.showSnackbar("Copied to clipboard") }
                    }
                )
            }

            split.result?.let { result ->
                SplitResultDialog(
                    result = result,
                    onDismiss = viewModel::dismissSplitResult,
                    onCopied = {
                        scope.launch { snackbarHostState.showSnackbar("Copied to clipboard") }
                    }
                )
            }

            if (showHistory) {
                HistorySheet(
                    history = history,
                    onDismiss = { showHistory = false },
                    onClear = viewModel::onClearHistory,
                    onRemove = viewModel::onRemoveHistory,
                    onCopy = {
                        scope.launch { snackbarHostState.showSnackbar("Copied to clipboard") }
                    }
                )
            }
        }
    }
}

@Composable
private fun AppHeader(
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    onHistoryClick: () -> Unit,
    historyCount: Int
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = tokens.spaceSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "Disk Partition\nCalculator",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                color = palette.contentPrimary
            )
        }
        GlassIconButton(
            icon = if (isDark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
            contentDescription = "Toggle theme",
            onClick = onToggleTheme,
            tint = palette.accent
        )
        Spacer(Modifier.width(8.dp))
        GlassIconButton(
            icon = Icons.Rounded.History,
            contentDescription = "History",
            onClick = onHistoryClick,
            tint = if (historyCount > 0) palette.accent else palette.contentSecondary
        )
    }
}
