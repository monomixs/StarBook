package com.starbook.features.folderPicker.addcontent

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.starbook.core.logging.api.Logger
import com.starbook.core.strings.R
import com.starbook.core.ui.icons.StarBookIcons
import com.starbook.features.folderPicker.folderPicker.FileTypeSelection

@Composable
internal fun SelectFolderButtonRow(onAdd: (FileTypeSelection, Uri) -> Unit) {
  Row(
    Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Center,
  ) {
    val openDocumentLauncher = rememberLauncherForActivityResult(
      ActivityResultContracts.OpenDocument(),
    ) { uri ->
      if (uri != null) {
        onAdd(FileTypeSelection.File, uri)
      }
    }
    val documentTreeLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
          onAdd(FileTypeSelection.Folder, uri)
        }
      }

    SelectFolderButton(
      icon = StarBookIcons.Folder,
      text = stringResource(id = R.string.folder_add_type_folder),
      onClick = {
        try {
          documentTreeLauncher.launch(null)
        } catch (e: ActivityNotFoundException) {
          Logger.w(e, "Could not add folder")
        }
      },
    )
    Spacer(modifier = Modifier.size(8.dp))
    SelectFolderButton(
      icon = StarBookIcons.AudioFile,
      text = stringResource(id = R.string.folder_add_type_file),
      onClick = {
        try {
          openDocumentLauncher.launch(arrayOf("*/*"))
        } catch (e: ActivityNotFoundException) {
          Logger.w(e, "Could not add file")
        }
      },
    )
  }
}

