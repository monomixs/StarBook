package com.starbook.features.playbackScreen.pixelplayer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import com.starbook.features.playbackScreen.pixelplayer.components.FullPlayerContent
import com.starbook.features.playbackScreen.pixelplayer.scoped.FullPlayerVisualState

@Composable
internal fun BoxScope.UnifiedPlayerMiniAndFullLayers(
  currentSong: Song?,
  playerContentExpansionFraction: Animatable<Float, AnimationVector1D>,
  fullPlayerVisualState: FullPlayerVisualState,
  containerHeight: Dp,
  playerViewModel: PixelPlayerViewModel,
  currentHorizontalPaddingStartPxProvider: () -> Float,
  currentHorizontalPaddingEndPxProvider: () -> Float,
  onOpenChapters: () -> Unit,
) {
  if (currentSong == null) return

  // Mini Player Layer
  val miniAlpha = (1f - playerContentExpansionFraction.value * 2f).coerceIn(0f, 1f)
  if (miniAlpha > 0f) {
    Box(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .fillMaxWidth()
        .height(MiniPlayerHeight)
        .graphicsLayer { alpha = miniAlpha }
        .layout { measurable, constraints ->
          val startPaddingPx = currentHorizontalPaddingStartPxProvider().toInt()
          val endPaddingPx = currentHorizontalPaddingEndPxProvider().toInt()
          val targetWidth = (constraints.maxWidth - startPaddingPx - endPaddingPx).coerceAtLeast(0)
          val placeable = measurable.measure(constraints.copy(minWidth = targetWidth, maxWidth = targetWidth))
          layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.placeRelative(startPaddingPx, 0)
          }
        }
        .zIndex(if (playerContentExpansionFraction.value < 0.5f) 1f else 0f),
    ) {
      val stableState by playerViewModel.stablePlayerState.collectAsState()
      MiniPlayerContentInternal(
        song = currentSong,
        isPlaying = stableState.isPlaying,
        isCastConnecting = false,
        isPreparingPlayback = false,
        onPlayPause = { playerViewModel.playPause() },
        onRewind = { playerViewModel.rewind() },
        onFastForward = { playerViewModel.fastForward() },
        modifier = Modifier.fillMaxSize(),
      )
    }
  }

  // Full Player Layer
  val fullAlpha = fullPlayerVisualState.contentAlpha
  if (fullAlpha > 0f) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .requiredHeight(containerHeight)
        .graphicsLayer {
          alpha = fullAlpha
          translationY = fullPlayerVisualState.translationY
        }
        .zIndex(if (playerContentExpansionFraction.value >= 0.5f) 1f else 0f),
    ) {
      FullPlayerContent(
        currentSong = currentSong,
        playerViewModel = playerViewModel,
        expansionFractionProvider = { playerContentExpansionFraction.value },
        onCollapse = { playerViewModel.collapsePlayerSheet() },
        onOpenChapters = onOpenChapters,
      )
    }
  }
}
