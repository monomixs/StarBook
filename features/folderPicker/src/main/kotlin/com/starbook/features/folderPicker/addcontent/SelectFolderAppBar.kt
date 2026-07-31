package com.starbook.features.folderPicker.addcontent

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.starbook.core.strings.R
import com.starbook.core.ui.icons.StarBookIcons

@Composable
internal fun SelectFolderAppBar(onBack: () -> Unit) {
  TopAppBar(
    title = { },
    navigationIcon = {
      IconButton(onClick = onBack) {
        Icon(
          imageVector = StarBookIcons.ArrowBack,
          contentDescription = stringResource(id = R.string.common_action_close),
        )
      }
    },
  )
}

