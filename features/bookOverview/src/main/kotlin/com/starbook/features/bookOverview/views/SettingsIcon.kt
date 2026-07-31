package com.starbook.features.bookOverview.views

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.starbook.core.ui.icons.StarBookIcons
import com.starbook.core.strings.R as StringsR

@Composable
internal fun SettingsIcon(onSettingsClick: () -> Unit) {
  IconButton(onSettingsClick) {
    Icon(
      imageVector = StarBookIcons.Settings,
      contentDescription = stringResource(StringsR.string.settings_action_open),
    )
  }
}

