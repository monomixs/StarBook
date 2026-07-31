package com.starbook.features.folderPicker

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.starbook.core.data.folders.FolderType
import com.starbook.core.ui.icons.StarBookIcons
import com.starbook.core.strings.R as StringsR

@Composable
internal fun FolderTypeIcon(folderType: FolderType) {
  Icon(
    imageVector = folderType.icon(),
    contentDescription = folderType.contentDescription(),
  )
}

private fun FolderType.icon(): ImageVector = when (this) {
  FolderType.SingleFile -> StarBookIcons.AudioFile
  FolderType.SingleFolder -> StarBookIcons.Folder
  FolderType.Root -> StarBookIcons.LibraryBooks
  FolderType.Author -> StarBookIcons.Person
}

@Composable
private fun FolderType.contentDescription(): String {
  val res = when (this) {
    FolderType.SingleFile,
    FolderType.SingleFolder,
    -> StringsR.string.folder_mode_single_title
    FolderType.Root -> StringsR.string.folder_mode_root_title
    FolderType.Author -> StringsR.string.folder_mode_author_title
  }
  return stringResource(res)
}

