package com.starbook.features.playbackScreen.pixelplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starbook.features.playbackScreen.pixelplayer.components.SmartImage
import com.starbook.features.playbackScreen.pixelplayer.components.AutoScrollingTextOnDemand

@Composable
internal fun MiniPlayerContentInternal(
  song: Song,
  isPlaying: Boolean,
  isCastConnecting: Boolean,
  isPreparingPlayback: Boolean,
  onPlayPause: () -> Unit,
  onRewind: () -> Unit,
  onFastForward: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val hapticFeedback = LocalHapticFeedback.current
  val controlsEnabled = !isCastConnecting && !isPreparingPlayback

  val rewindInteraction = remember { MutableInteractionSource() }
  val playPauseInteraction = remember { MutableInteractionSource() }
  val ffInteraction = remember { MutableInteractionSource() }
  val miniPlayerIndication = remember { ripple(bounded = false) }

  val colorScheme = MaterialTheme.colorScheme

  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(MiniPlayerHeight)
      .padding(start = 10.dp, end = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val albumArtModel = song.albumArtUriString?.takeIf { it.isNotBlank() }
    Box(contentAlignment = Alignment.Center) {
      SmartImage(
        model = albumArtModel,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.size(44.dp),
      )
      if (isCastConnecting) {
        CircularProgressIndicator(
          modifier = Modifier.size(24.dp),
          strokeWidth = 2.dp,
          color = colorScheme.onSurface,
        )
      }
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.Center,
    ) {
      val titleStyle = MaterialTheme.typography.titleSmall.copy(
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.2).sp,
        color = colorScheme.onSurface,
      )
      val artistStyle = MaterialTheme.typography.bodySmall.copy(
        fontSize = 13.sp,
        letterSpacing = 0.sp,
        color = colorScheme.onSurface.copy(alpha = 0.7f),
      )

      AutoScrollingTextOnDemand(
        text = when {
          isCastConnecting -> "Connecting..."
          isPreparingPlayback -> "Preparing..."
          else -> song.title
        },
        style = titleStyle,
        gradientEdgeColor = colorScheme.surfaceContainer,
      )
      AutoScrollingTextOnDemand(
        text = if (isPreparingPlayback) "Loading..." else song.displayArtist,
        style = artistStyle,
        gradientEdgeColor = colorScheme.surfaceContainer,
      )
    }
    Spacer(modifier = Modifier.width(8.dp))
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .background(colorScheme.surfaceVariant)
        .clickable(
          interactionSource = rewindInteraction,
          indication = miniPlayerIndication,
          enabled = controlsEnabled,
        ) {
          hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onRewind()
        },
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = PixelPlayerIcons.FastRewind,
        contentDescription = "Rewind",
        tint = colorScheme.onSurfaceVariant,
        modifier = Modifier.size(18.dp),
      )
    }

    Spacer(modifier = Modifier.width(8.dp))

    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .background(colorScheme.primary)
        .clickable(
          interactionSource = playPauseInteraction,
          indication = miniPlayerIndication,
          enabled = controlsEnabled,
        ) {
          hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onPlayPause()
        },
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = if (isPlaying) PixelPlayerIcons.Pause else PixelPlayerIcons.PlayArrow,
        contentDescription = if (isPlaying) "Pause" else "Play",
        tint = colorScheme.onPrimary,
        modifier = Modifier.size(18.dp),
      )
    }

    Spacer(modifier = Modifier.width(8.dp))

    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .background(colorScheme.surfaceVariant)
        .clickable(
          interactionSource = ffInteraction,
          indication = miniPlayerIndication,
          enabled = controlsEnabled,
        ) { onFastForward() },
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = PixelPlayerIcons.FastForward,
        contentDescription = "Forward",
        tint = colorScheme.onSurfaceVariant,
        modifier = Modifier.size(18.dp),
      )
    }
  }
}
