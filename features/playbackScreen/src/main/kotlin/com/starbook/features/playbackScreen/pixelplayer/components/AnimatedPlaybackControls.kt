package com.starbook.features.playbackScreen.pixelplayer.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.starbook.features.playbackScreen.pixelplayer.PixelPlayerIcons
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedPlaybackControls(
    modifier: Modifier = Modifier,
    isPlayingProvider: () -> Boolean,
    isBufferingProvider: () -> Boolean = { false },
    onRewind: () -> Unit,
    onPlayPause: () -> Unit,
    onFastForward: () -> Unit,
    height: Dp = 80.dp,
    colorOtherButtons: Color = MaterialTheme.colorScheme.secondaryContainer,
    colorPlayPause: Color = MaterialTheme.colorScheme.primaryContainer,
    tintPlayPauseIcon: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    tintOtherIcons: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    val scope = rememberCoroutineScope()
    val isPlaying = isPlayingProvider()
    val isBuffering = isBufferingProvider()
    val haptic = LocalHapticFeedback.current

    var activeBtn by remember { mutableStateOf(PlaybackButtonType.NONE) }

    val rewindWeight by animateFloatAsState(
        targetValue = if (activeBtn == PlaybackButtonType.REWIND) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "rewindWeight"
    )
    val playPauseWeight by animateFloatAsState(
        targetValue = if (activeBtn == PlaybackButtonType.PLAY_PAUSE) 1.5f else 1.2f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "playPauseWeight"
    )
    val ffWeight by animateFloatAsState(
        targetValue = if (activeBtn == PlaybackButtonType.FF) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ffWeight"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .height(height),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rewind
        Box(
            modifier = Modifier
                .weight(rewindWeight)
                .fillMaxHeight()
                .clip(RoundedCornerShape(60.dp))
                .background(colorOtherButtons)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    scope.launch {
                        activeBtn = PlaybackButtonType.REWIND
                        onRewind()
                        delay(220)
                        activeBtn = PlaybackButtonType.NONE
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = PixelPlayerIcons.FastRewind,
                contentDescription = "Rewind",
                tint = tintOtherIcons,
                modifier = Modifier.size(24.dp)
            )
        }

        // Play/Pause
        Box(
            modifier = Modifier
                .weight(playPauseWeight)
                .fillMaxHeight()
                .clip(RoundedCornerShape(60.dp))
                .background(colorPlayPause)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    scope.launch {
                        activeBtn = PlaybackButtonType.PLAY_PAUSE
                        onPlayPause()
                        delay(220)
                        activeBtn = PlaybackButtonType.NONE
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Crossfade(targetState = isBuffering, label = "playPauseContent") { buffering ->
                if (buffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = tintPlayPauseIcon,
                        strokeWidth = 3.dp,
                        strokeCap = StrokeCap.Round
                    )
                } else {
                    Icon(
                        imageVector = if (isPlaying) PixelPlayerIcons.Pause else PixelPlayerIcons.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = tintPlayPauseIcon,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Fast Forward
        Box(
            modifier = Modifier
                .weight(ffWeight)
                .fillMaxHeight()
                .clip(RoundedCornerShape(60.dp))
                .background(colorOtherButtons)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    scope.launch {
                        activeBtn = PlaybackButtonType.FF
                        onFastForward()
                        delay(220)
                        activeBtn = PlaybackButtonType.NONE
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = PixelPlayerIcons.FastForward,
                contentDescription = "Forward",
                tint = tintOtherIcons,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private enum class PlaybackButtonType {
    NONE, REWIND, PLAY_PAUSE, FF
}
