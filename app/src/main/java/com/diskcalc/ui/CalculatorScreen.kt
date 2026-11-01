package com.diskcalc.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.diskcalc.ui.components.GlassDivider
import com.diskcalc.ui.components.GlassIconButton
import com.diskcalc.ui.components.GlassTabBar
import com.diskcalc.ui.components.GlassTabItem
import com.diskcalc.ui.theme.DiskCalcTheme
import com.diskcalc.ui.theme.LiquidBackdropHost
import com.diskcalc.ui.theme.LocalGlassTokens
import com.diskcalc.ui.theme.LocalLiquidPalette

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
    var infoPageStack by remember { mutableStateOf<List<String>>(emptyList()) }
    val currentInfoPage = infoPageStack.lastOrNull()
    val openInfoPage: (String) -> Unit = { page ->
        infoPageStack = if (infoPageStack.lastOrNull() == page) infoPageStack else infoPageStack + page
    }
    val closeInfoPage: () -> Unit = {
        if (infoPageStack.isNotEmpty()) {
            infoPageStack = infoPageStack.dropLast(1)
        }
    }
    var showHistory by remember { mutableStateOf(false) }

    val tabs = remember {
        listOf(
            GlassTabItem(title = "Calc", icon = Icons.Rounded.Calculate),
            GlassTabItem(title = "Split", icon = Icons.Rounded.Dashboard),
            GlassTabItem(title = "Settings", icon = Icons.Rounded.Settings)
        )
    }

    // Back on any non-start tab returns to Calc; back inside an info page steps back cleanly.
    BackHandler(enabled = selectedTabIndex != 0) { selectedTabIndex = 0 }
    BackHandler(enabled = currentInfoPage != null) { closeInfoPage() }

    DiskCalcTheme(themeMode = settings.themeMode) {
        LiquidBackdropHost(modifier = modifier) {
            val tokens = LocalGlassTokens.current

            Box(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    Column(
                        Modifier
                            .widthIn(max = tokens.maxContentWidth)
                            .fillMaxHeight()
                            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                            .padding(horizontal = (tokens.screenPadding - 6.dp).coerceAtLeast(0.dp))
                    ) {
                        if (currentInfoPage == null) {
                            AppHeader(
                                onHistoryClick = { showHistory = true },
                                historyCount = history.size,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            Spacer(Modifier.height(tokens.spaceSm))
                            GlassDivider(faded = true, modifier = Modifier.padding(horizontal = 6.dp))
                            Spacer(Modifier.height(tokens.spaceMd))
                        }

                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            // Tabs and info pages crossfade instead of snapping, so switching
                            // screens reads as one continuous surface.
                            Crossfade(
                                targetState = currentInfoPage ?: selectedTabIndex,
                                animationSpec = tween(
                                    durationMillis = 220,
                                    easing = FastOutSlowInEasing
                                ),
                                label = "screenFade"
                            ) { screen ->
                                when (screen) {
                                    "privacy" -> PrivacyPolicyPage(onBack = closeInfoPage)
                                    "guide" -> GuidePage(onBack = closeInfoPage)
                                    "about" -> AboutPage(
                                        onBack = closeInfoPage,
                                        onCopied = {}
                                    )

                                    0 -> CalcTab(
                                        state = calc,
                                        onVisibleGbChange = viewModel::onVisibleGbChange,
                                        onPresetSelected = viewModel::applyPreset,
                                        onFilesystemChange = viewModel::onCalcFilesystemChange,
                                        onSetupToggle = viewModel::onCalcSetupToggle,
                                        onProfileChange = viewModel::onSetupProfileChange,
                                        onCustomOverheadChange = viewModel::onCustomOverheadChange,
                                        onCalculate = viewModel::calculate,
                                        onCopied = {}
                                    )

                                    1 -> SplitTab(
                                        state = split,
                                        onTotalMbChange = viewModel::onTotalMbChange,
                                        onPartitionCountChange = viewModel::onPartitionCountChange,
                                        onFilesystemChange = viewModel::onSplitFilesystemChange,
                                        onSplit = { viewModel.split() },
                                        onCopied = {}
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
                                        onOpenPrivacyPolicy = { openInfoPage("privacy") },
                                        onOpenGuide = { openInfoPage("guide") },
                                        onOpenAbout = { openInfoPage("about") }
                                    )
                                }
                            }
                        }
                    }
                }

                // Floating Type Navigation Bar
                if (currentInfoPage == null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .widthIn(max = tokens.maxContentWidth)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(
                                start = tokens.screenPadding + 4.dp,
                                end = tokens.screenPadding + 4.dp,
                                bottom = 12.dp
                            )
                    ) {
                        GlassTabBar(
                            items = tabs,
                            selectedIndex = selectedTabIndex,
                            onSelect = { selectedTabIndex = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

            }

            // ------------------------------------------------------------ dialogs
            calc.result?.let { result ->
                CalcResultDialog(
                    result = result,
                    onDismiss = viewModel::dismissCalcResult,
                    onCopied = {}
                )
            }

            split.result?.let { result ->
                SplitResultDialog(
                    result = result,
                    onDismiss = viewModel::dismissSplitResult,
                    onCopied = {}
                )
            }

            if (showHistory) {
                HistorySheet(
                    history = history,
                    onDismiss = { showHistory = false },
                    onClear = viewModel::onClearHistory,
                    onRemove = viewModel::onRemoveHistory,
                    onCopy = {}
                )
            }
        }
    }
}

@Composable
private fun AppHeader(
    onHistoryClick: () -> Unit,
    historyCount: Int,
    modifier: Modifier = Modifier
) {
    val palette = LocalLiquidPalette.current
    val tokens = LocalGlassTokens.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = tokens.spaceSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BoxWithConstraints(Modifier.weight(1f).padding(end = tokens.spaceSm)) {
            val title = "Disk Partition Calculator"
            val style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold)
            val measurer = rememberTextMeasurer()
            val widthPx = with(LocalDensity.current) { maxWidth.roundToPx() }
            // Use the largest size that fits beside the history button, including at larger
            // system font scales. Ellipsis remains a fallback for exceptionally narrow screens.
            val fontSize = (28 downTo 12).firstOrNull { size ->
                !measurer.measure(
                    text = title,
                    style = style.copy(fontSize = size.sp),
                    maxLines = 1,
                    softWrap = false,
                    constraints = Constraints(maxWidth = widthPx)
                ).hasVisualOverflow
            } ?: 12
            Text(
                text = title,
                style = style.copy(fontSize = fontSize.sp),
                color = palette.contentPrimary,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
        GlassIconButton(
            icon = Icons.Rounded.History,
            contentDescription = "History",
            onClick = onHistoryClick,
            tint = palette.contentSecondary
        )
    }
}
