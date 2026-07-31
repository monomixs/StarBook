package com.starbook.features.playbackScreen.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.starbook.core.strings.R
import com.starbook.core.ui.icons.StarBookIcons

@Composable
internal fun SkipButton(
  forward: Boolean,
  onClick: () -> Unit,
) {
  Icon(
    modifier = Modifier
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = false),
        onClick = onClick,
      )
      .size(48.dp)
      .scale(scaleX = if (forward) -1f else 1F, scaleY = 1f),
    imageVector = StarBookIcons.Undo,
    contentDescription = stringResource(
      id = if (forward) {
        R.string.playback_action_fast_forward
      } else {
        R.string.playback_action_rewind
      },
    ),
  )
}

