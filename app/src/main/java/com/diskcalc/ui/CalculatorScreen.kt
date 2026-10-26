package com.diskcalc.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
 * All business state lives in [CalculatorViewModel]. Theme switching is animated inside
 * [DiskCalcTheme] (300 ms token crossfade) rather than by swapping the whole content tree.
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
    var infoPage by remember { mutableStateOf<String?>(null) }
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

    // Back on any non-start tab returns to Calc; back inside an info page closes the page.
    BackHandler(enabled = selectedTabIndex != 0) { selectedTabIndex = 0 }
    BackHandler(enabled = infoPage != null) { infoPage = null }

    DiskCalcTheme(themeMode = settings.themeMode) {
        LiquidBackdropHost(modifier = modifier) {
            val palette = LocalLiquidPalette.current
            val tokens = LocalGlassTokens.current

            Box(
                Modifier
                    .fillMaxSize()
                    .background(palette.backdropBase)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    Column(
                        Modifier
                            .widthIn(max = tokens.maxContentWidth)
                            .fillMaxHeight()
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                            .padding(horizontal = tokens.screenPadding)
                    ) {
                        if (infoPage == null) {
                            AppHeader(
                                onHistoryClick = { showHistory = true },
                                historyCount = history.size
                            )
                            Spacer(Modifier.height(tokens.spaceMd))
                        }

                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            when (infoPage) {
                                "privacy" -> PrivacyPolicyPage(onBack = { infoPage = null })
                                "guide" -> GuidePage(onBack = { infoPage = null })
                                "about" -> AboutPage(onBack = { infoPage = null })
                                null -> when (selectedTabIndex) {
                                    0 -> CalcTab(
                                        state = calc,
                                        onVisibleGbChange = viewModel::onVisibleGbChange,
                                        onPresetSelected = viewModel::applyPreset,
                                        onFilesystemChange = viewModel::onCalcFilesystemChange,
                                        onSetupToggle = viewModel::onCalcSetupToggle,
                                        onProfileChange = viewModel::onSetupProfileChange,
                                        onCustomOverheadChange = viewModel::onCustomOverheadChange,
                                        onCalculate = viewModel::calculate,
                                        onCopied = {
                                            scope.launch { snackbarHostState.showSnackbar("Copied") }
                                        }
                                    )

                                    1 -> SplitTab(
                                        state = split,
                                        onTotalMbChange = viewModel::onTotalMbChange,
                                        onPartitionCountChange = viewModel::onPartitionCountChange,
                                        onFilesystemChange = viewModel::onSplitFilesystemChange,
                                        onSetupToggle = viewModel::onSplitSetupToggle,
                                        onSplit = { viewModel.split() },
                                        onCopied = {
                                            scope.launch { snackbarHostState.showSnackbar("Copied") }
                                        }
                                    )

                                    else -> SettingsTab(
                                        settings = settings,
                                        historySize = history.size,
                                        onThemeChange = viewModel::onThemeChange,
                                        onDefaultFilesystemChange = viewModel::onDefaultFilesystemChange,
                                        onDefaultSetupChange = viewModel::onDefaultSetupChange,
                                        onDefaultProfileChange = viewModel::onDefaultProfileChange,
                                        onClearHistory = viewModel::onClearHistory,
                                        onShowHistory = { showHistory = true },
                                        onOpenPrivacyPolicy = { infoPage = "privacy" },
                                        onOpenGuide = { infoPage = "guide" },
                                        onOpenAbout = { infoPage = "about" }
                                    )
                                }
                            }
                        }

                        if (infoPage == null) {
                            Spacer(Modifier.height(tokens.spaceSm))
                            GlassTabBar(
                                items = tabs,
                                selectedIndex = selectedTabIndex,
                                onSelect = { selectedTabIndex = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(tokens.spaceSm))
                        }
                    }
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .widthIn(max = tokens.maxContentWidth)
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(
                            bottom = tokens.spaceXxl + tokens.touchTarget + tokens.spaceMd,
                            start = tokens.spaceXl,
                            end = tokens.spaceXl
                        )
                ) { data ->
                    LiquidGlassSurface(
                        shape = RoundedCornerShape(percent = 50),
                        tint = palette.glassTint,
                        elevation = tokens.elevationSmall
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = tokens.spaceXl,
                                vertical = tokens.spaceMd
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = data.visuals.message,
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.contentPrimary,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            val actionLabel = data.visuals.actionLabel
                            if (actionLabel != null) {
                                Spacer(Modifier.width(tokens.spaceMd))
                                Text(
                                    text = actionLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = palette.accent,
                                    modifier = Modifier
                                        .heightIn(min = tokens.touchTarget)
                                        .clickable { data.performAction() }
                                        .padding(vertical = tokens.spaceMd)
                                )
                            }
                        }
                    }
                }
            }

            // ------------------------------------------------------------ dialogs
            calc.result?.let { result ->
                CalcResultDialog(
                    result = result,
                    onDismiss = viewModel::dismissCalcResult,
                    onCopied = {
                        scope.launch { snackbarHostState.showSnackbar("Copied") }
                    }
                )
            }

            split.result?.let { result ->
                SplitResultDialog(
                    result = result,
                    onDismiss = viewModel::dismissSplitResult,
                    onCopied = {
                        scope.launch { snackbarHostState.showSnackbar("Copied") }
                    }
                )
            }

            if (showHistory) {
                HistorySheet(
                    history = history,
                    onDismiss = { showHistory = false },
                    onClear = viewModel::onClearHistory,
                    onRemove = { id ->
                        viewModel.onRemoveHistory(id)
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Deleted",
                                actionLabel = "Undo",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.undoRemoveHistory()
                            }
                        }
                    },
                    onCopy = {
                        scope.launch { snackbarHostState.showSnackbar("Copied") }
                    },
                    onGoToCalc = {
                        showHistory = false
                        selectedTabIndex = 0
                    }
                )
            }
        }
    }
}

@Composable
private fun AppHeader(
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
            icon = Icons.Rounded.History,
            contentDescription = "History",
            onClick = onHistoryClick,
            tint = if (historyCount > 0) palette.accent else palette.contentSecondary
        )
    }
}
