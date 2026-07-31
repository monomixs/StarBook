package com.starbook.features.playbackScreen.pixelplayer.scoped

import androidx.compose.runtime.*
import kotlinx.coroutines.isActive
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun rememberSmoothProgress(
    isPlayingProvider: () -> Boolean,
    currentPositionProvider: () -> Long,
    totalDuration: Long,
    sampleWhilePlayingMs: Long = 180L,
    sampleWhilePausedMs: Long = 800L,
    isVisible: Boolean = true
): Pair<State<Float>, () -> Unit> {
    val progress = remember { mutableFloatStateOf(0f) }

    val currentPosition = currentPositionProvider()
    val isPlaying = isPlayingProvider()

    LaunchedEffect(currentPosition, isPlaying, totalDuration, isVisible) {
        if (!isVisible || totalDuration <= 0) return@LaunchedEffect

        val targetProgress = currentPosition.toFloat() / totalDuration.toFloat()
        if (isPlaying) {
             val startTime = System.currentTimeMillis()
             while (true) {
                 val elapsed = System.currentTimeMillis() - startTime
                 val addedProgress = elapsed.toFloat() / totalDuration.toFloat()
                 progress.floatValue = (targetProgress + addedProgress).coerceIn(0f, 1f)
                 withFrameNanos { it }
             }
        } else {
            progress.floatValue = targetProgress.coerceIn(0f, 1f)
        }
    }

    return progress to {}
}
