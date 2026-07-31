package com.starbook.features.playbackScreen.view

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.starbook.core.strings.R
import com.starbook.core.ui.icons.StarBookIcons

@Composable
internal fun CloseIcon(onCloseClick: () -> Unit) {
  IconButton(onClick = onCloseClick) {
    Icon(
      imageVector = StarBookIcons.Close,
      contentDescription = stringResource(id = R.string.common_action_close),
    )
  }
}

