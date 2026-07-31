package com.starbook.features.playbackScreen.pixelplayer

import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.starbook.features.playbackScreen.pixelplayer.scoped.*
import com.starbook.features.playbackScreen.BookPlayDialogViewState
import com.starbook.features.playbackScreen.pixelplayer.components.SpeedDialog
import com.starbook.features.playbackScreen.pixelplayer.components.VolumeGainDialog
import com.starbook.features.playbackScreen.pixelplayer.components.SelectChapterDialog
import com.starbook.features.sleepTimer.SleepTimerDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UnifiedPlayerSheetV2(
    playerViewModel: PixelPlayerViewModel,
    sheetCollapsedTargetY: Float,
    containerHeight: Dp,
    collapsedStateHorizontalPadding: Dp = 12.dp,
    onOpenChapters: () -> Unit
) {
    val infrequentPlayerState by playerViewModel.stablePlayerState.collectAsState()
    val currentSheetContentState by playerViewModel.sheetState.collectAsState()
    val predictiveBackCollapseProgress by playerViewModel.predictiveBackCollapseFraction.collectAsState()

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()

    val miniPlayerContentHeightPx = with(density) { MiniPlayerHeight.toPx() }

    val playerContentExpansionFraction = playerViewModel.playerContentExpansionFraction
    val showPlayerContentArea = infrequentPlayerState.currentSong != null ||
                                currentSheetContentState == PlayerSheetState.EXPANDED ||
                                playerContentExpansionFraction.value > 0.01f

    val visualOvershootScaleY = remember { Animatable(1f) }
    val initialFullPlayerOffsetY = with(density) { 24.dp.toPx() }
    val motionScheme = MotionScheme.expressive()
    val sheetAnimationSpec = motionScheme.defaultSpatialSpec<Float>()
    val sheetAnimationMutex = remember { MutatorMutex() }
    val sheetExpandedTargetY = 0f

    val currentSheetTranslationY = remember {
        val initialY = if (currentSheetContentState == PlayerSheetState.COLLAPSED) sheetCollapsedTargetY else sheetExpandedTargetY
        Animatable(initialY)
    }

    val sheetMotionController = remember(
        currentSheetTranslationY,
        playerContentExpansionFraction,
        sheetAnimationMutex,
        sheetAnimationSpec
    ) {
        SheetMotionController(
            translationY = currentSheetTranslationY,
            expansionFraction = playerContentExpansionFraction,
            mutex = sheetAnimationMutex,
            defaultAnimationSpec = sheetAnimationSpec,
            expandedY = sheetExpandedTargetY
        )
    }

    suspend fun animatePlayerSheet(targetExpanded: Boolean, initialVelocity: Float = 0f) {
        sheetMotionController.animateTo(
            targetExpanded = targetExpanded,
            canExpand = showPlayerContentArea,
            collapsedY = sheetCollapsedTargetY,
            initialVelocity = initialVelocity
        )
    }

    LaunchedEffect(sheetCollapsedTargetY) {
        sheetMotionController.syncToExpansion(sheetCollapsedTargetY)
    }

    LaunchedEffect(showPlayerContentArea, currentSheetContentState) {
        val targetExpanded = showPlayerContentArea && currentSheetContentState == PlayerSheetState.EXPANDED
        animatePlayerSheet(targetExpanded = targetExpanded)
    }

    val sheetVisualState = rememberSheetVisualState(
        showPlayerContentArea = showPlayerContentArea,
        collapsedStateHorizontalPadding = collapsedStateHorizontalPadding,
        predictiveBackCollapseProgress = predictiveBackCollapseProgress,
        predictiveBackSwipeEdge = null,
        currentSheetContentState = currentSheetContentState,
        playerContentExpansionFraction = playerContentExpansionFraction,
        containerHeight = containerHeight,
        currentSheetTranslationY = currentSheetTranslationY,
        sheetCollapsedTargetY = sheetCollapsedTargetY,
        navBarStyle = NavBarStyle.DEFAULT,
        navBarCornerRadiusDp = 32.dp,
        isNavBarHidden = false,
        isPlaying = infrequentPlayerState.isPlaying,
        hasCurrentSong = infrequentPlayerState.currentSong != null,
        swipeDismissProgress = 0f
    )

    val sheetInteractionState = rememberSheetInteractionState(
        scope = scope,
        velocityTracker = remember { androidx.compose.ui.input.pointer.util.VelocityTracker() },
        sheetMotionController = sheetMotionController,
        playerContentExpansionFraction = playerContentExpansionFraction,
        currentSheetTranslationY = currentSheetTranslationY,
        visualOvershootScaleY = visualOvershootScaleY,
        sheetCollapsedTargetY = sheetCollapsedTargetY,
        sheetExpandedTargetY = sheetExpandedTargetY,
        miniPlayerContentHeightPx = miniPlayerContentHeightPx,
        currentSheetContentState = currentSheetContentState,
        showPlayerContentArea = showPlayerContentArea,
        overallSheetTopCornerRadiusProvider = sheetVisualState.overallSheetTopCornerRadiusProvider,
        playerContentActualBottomRadiusProvider = sheetVisualState.playerContentActualBottomRadiusProvider,
        useSmoothCorners = false,
        isDragging = false,
        onAnimateSheet = { target, _, vel -> animatePlayerSheet(target, vel) },
        onExpandSheetState = { playerViewModel.expandPlayerSheet() },
        onCollapseSheetState = { playerViewModel.collapsePlayerSheet() },
        onDraggingChange = {},
        onDraggingPlayerAreaChange = {}
    )

    val fullPlayerVisualState = rememberFullPlayerVisualState(
        expansionFraction = playerContentExpansionFraction,
        initialOffsetY = initialFullPlayerOffsetY
    )

    if (showPlayerContentArea) {
        PredictiveBackHandler(enabled = currentSheetContentState == PlayerSheetState.EXPANDED) { progressFlow ->
            try {
                progressFlow.collect { backEvent ->
                    playerViewModel.updatePredictiveBackCollapseFraction(backEvent.progress)
                }
                playerViewModel.collapsePlayerSheet()
            } catch (_: Exception) {
                playerViewModel.updatePredictiveBackCollapseFraction(0f)
            } finally {
                playerViewModel.updatePredictiveBackCollapseFraction(0f)
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .layout { measurable, constraints ->
                    val targetHeight = sheetVisualState.playerContentAreaHeightPxProvider().toInt()
                    val placeable = measurable.measure(constraints.copy(minHeight = targetHeight, maxHeight = targetHeight))
                    layout(constraints.maxWidth, targetHeight) {
                        placeable.placeRelative(0, 0)
                    }
                }
                .graphicsLayer {
                    translationY = sheetVisualState.visualSheetTranslationYProvider()
                },
            color = Color.Transparent
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleY = visualOvershootScaleY.value
                            transformOrigin = TransformOrigin(0.5f, 1f)
                        }
                        .layout { measurable, constraints ->
                            val targetHeightPx = constraints.maxHeight
                            val startPaddingPx = sheetVisualState.currentHorizontalPaddingStartPxProvider().toInt()
                            val endPaddingPx = sheetVisualState.currentHorizontalPaddingEndPxProvider().toInt()
                            val innerWidth = (constraints.maxWidth - startPaddingPx - endPaddingPx).coerceAtLeast(0)
                            val placeable = measurable.measure(constraints.copy(minWidth = innerWidth, maxWidth = innerWidth, minHeight = targetHeightPx, maxHeight = targetHeightPx))
                            layout(constraints.maxWidth, targetHeightPx) {
                                placeable.placeRelative(startPaddingPx, 0)
                            }
                        }
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = sheetInteractionState.playerShadowShape
                        )
                        .clip(sheetInteractionState.playerShadowShape)
                        .playerSheetVerticalDragGesture(
                            enabled = sheetInteractionState.canDragSheet,
                            handler = sheetInteractionState.sheetVerticalDragGestureHandler
                        )
                        .clickable(
                            enabled = currentSheetContentState == PlayerSheetState.COLLAPSED,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            playerViewModel.togglePlayerSheetState()
                        }
                ) {
                    UnifiedPlayerMiniAndFullLayers(
                        currentSong = infrequentPlayerState.currentSong,
                        playerContentExpansionFraction = playerContentExpansionFraction,
                        fullPlayerVisualState = fullPlayerVisualState,
                        containerHeight = containerHeight,
                        playerViewModel = playerViewModel,
                        currentHorizontalPaddingStartPxProvider = sheetVisualState.currentHorizontalPaddingStartPxProvider,
                        currentHorizontalPaddingEndPxProvider = sheetVisualState.currentHorizontalPaddingEndPxProvider,
                        onOpenChapters = onOpenChapters
                    )
                }
            }
        }

        val dialogState = playerViewModel.dialogState.value
        if (dialogState != null) {
            when (dialogState) {
                is BookPlayDialogViewState.SpeedDialog -> {
                    SpeedDialog(dialogState, playerViewModel::onPlaybackSpeedChanged) {
                        playerViewModel.dismissDialog()
                    }
                }
                is BookPlayDialogViewState.VolumeGainDialog -> {
                    VolumeGainDialog(dialogState, playerViewModel::onVolumeGainChanged) {
                        playerViewModel.dismissDialog()
                    }
                }
                is BookPlayDialogViewState.SelectChapterDialog -> {
                    SelectChapterDialog(dialogState, playerViewModel::onChapterClick) {
                        playerViewModel.dismissDialog()
                    }
                }
                is BookPlayDialogViewState.SleepTimer -> {
                    SleepTimerDialog(
                        viewState = dialogState.viewState,
                        onDismiss = playerViewModel::dismissDialog,
                        onIncrementSleepTime = playerViewModel::incrementSleepTime,
                        onDecrementSleepTime = playerViewModel::decrementSleepTime,
                        onAcceptSleepTime = playerViewModel::onAcceptSleepTime,
                        onAcceptSleepAtEndOfChapter = playerViewModel::onAcceptSleepAtEndOfChapter,
                    )
                }
            }
        }
    }
}
