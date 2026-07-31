package com.starbook.features.playbackScreen.pixelplayer.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WavySliderExpressive(
    value: () -> Float,
    onValueChange: (Float) -> Unit,
    onValueCommit: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    activeTrackColor: Color = MaterialTheme.colorScheme.primary,
    inactiveTrackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    thumbColor: Color = MaterialTheme.colorScheme.primary,
    isPlaying: Boolean = true,
    isVisible: Boolean = true,
    strokeWidth: Dp = 5.dp,
    thumbRadius: Dp = 8.dp,
    trackEdgePadding: Dp = thumbRadius,
    wavelength: Dp = WavyProgressIndicatorDefaults.LinearDeterminateWavelength,
    waveSpeed: Dp = WavyProgressIndicatorDefaults.LinearDeterminateWavelength / 2f,
) {
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { strokeWidth.toPx() }
    val thumbRadiusPx = with(density) { thumbRadius.toPx() }
    val trackEdgePaddingPx = with(density) { trackEdgePadding.coerceAtLeast(0.dp).toPx() }

    var isDragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableFloatStateOf(0f) }

    val currentDisplayValue = if (isDragging) dragValue else value()

    val stroke = remember(strokeWidthPx) {
        Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
    }

    val animatedAmplitude by animateFloatAsState(
        targetValue = if (enabled && isPlaying) 1f else 0f,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "amplitude"
    )

    val containerHeight = max(WavyProgressIndicatorDefaults.LinearContainerHeight, thumbRadius * 2)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(containerHeight)
            .pointerInput(enabled, trackEdgePaddingPx) {
                if (!enabled) return@pointerInput
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val trackStart = trackEdgePaddingPx
                        val trackEnd = size.width - trackEdgePaddingPx
                        val trackWidth = (trackEnd - trackStart).coerceAtLeast(0f)
                        if (trackWidth > 0) {
                            dragValue = ((offset.x - trackStart) / trackWidth).coerceIn(0f, 1f)
                            onValueChange(dragValue)
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val trackStart = trackEdgePaddingPx
                        val trackEnd = size.width - trackEdgePaddingPx
                        val trackWidth = (trackEnd - trackStart).coerceAtLeast(0f)
                        if (trackWidth > 0) {
                            dragValue = (dragValue + (dragAmount.x / trackWidth)).coerceIn(0f, 1f)
                            onValueChange(dragValue)
                        }
                    },
                    onDragEnd = {
                        isDragging = false
                        onValueCommit(dragValue)
                    },
                    onDragCancel = {
                        isDragging = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (isVisible) {
            LinearWavyProgressIndicator(
                progress = { currentDisplayValue.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = trackEdgePadding.coerceAtLeast(0.dp)),
                color = activeTrackColor,
                trackColor = inactiveTrackColor,
                stroke = stroke,
                trackStroke = stroke,
                amplitude = { progress -> if (progress > 0f) animatedAmplitude else 0f },
                wavelength = wavelength,
                waveSpeed = waveSpeed
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            if (!isVisible) return@Canvas
            val trackStart = trackEdgePaddingPx
            val trackEnd = size.width - trackEdgePaddingPx
            val trackWidth = (trackEnd - trackStart).coerceAtLeast(0f)
            val thumbY = size.height / 2
            val renderedProgress = currentDisplayValue.coerceIn(0f, 1f)

            val thumbX = trackStart + (trackWidth * renderedProgress)

            drawCircle(
                color = thumbColor,
                radius = thumbRadiusPx,
                center = Offset(thumbX, thumbY)
            )
        }
    }
}
