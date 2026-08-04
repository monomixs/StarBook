package com.starbook.features.folderPicker.addcontent

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.starbook.core.ui.ChoiceCard
import com.starbook.core.ui.SetupBackground
import com.starbook.core.ui.icons.StarBookIcons
import com.starbook.features.folderPicker.folderPicker.FileTypeSelection
import com.starbook.navigation.Origin
import com.starbook.core.strings.R as StringsR

@Composable
internal fun SelectFolder(
  onBack: () -> Unit,
  onAdd: (FileTypeSelection, Uri) -> Unit,
  origin: Origin,
  modifier: Modifier = Modifier,
) {
  val isDark = isSystemInDarkTheme()
  val surfaceColor = if (isDark) Color(0xFF232634) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
  val onSurfaceColor = if (isDark) Color(0xFFECEBF4) else MaterialTheme.colorScheme.onSurface
  val onSurfaceVarColor = if (isDark) Color(0xFFAFB0C4) else MaterialTheme.colorScheme.onSurfaceVariant

  val fileLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument(),
    onResult = { uri: Uri? ->
      if (uri != null) {
        onAdd(FileTypeSelection.File, uri)
      }
    }
  )

  val folderLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocumentTree(),
    onResult = { uri: Uri? ->
      if (uri != null) {
        onAdd(FileTypeSelection.Folder, uri)
      }
    }
  )

  SetupBackground(modifier = modifier) {
    Scaffold(
      containerColor = Color.Transparent,
      topBar = {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onBack,
            modifier = Modifier
              .size(40.dp)
              .background(surfaceColor, androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
          ) {
            Icon(
              imageVector = StarBookIcons.ArrowBack,
              contentDescription = "Back",
              tint = onSurfaceColor
            )
          }
        }
      }
    ) { padding ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
          .padding(26.dp)
      ) {
        Text(
          text = stringResource(
            when (origin) {
              Origin.Default -> StringsR.string.folder_add_title_default
              Origin.Onboarding -> StringsR.string.folder_add_title_onboarding
            },
          ),
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.ExtraBold,
          color = onSurfaceColor
        )
        Text(
          text = stringResource(StringsR.string.folder_add_type_subtitle),
          style = MaterialTheme.typography.bodyLarge,
          color = onSurfaceVarColor,
          modifier = Modifier.padding(top = 14.dp)
        )

        Spacer(Modifier.weight(0.2f))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          ChoiceCard(
            label = "Folder",
            icon = StarBookIcons.Folder,
            accentColor = Color(0xFF8FE3C4),
            containerColor = Color(0xFF1E4C3C),
            surfaceColor = surfaceColor,
            onSurfaceColor = onSurfaceColor,
            onClick = { folderLauncher.launch(null) },
            modifier = Modifier.weight(1f)
          )
          ChoiceCard(
            label = "File",
            icon = StarBookIcons.AudioFile,
            accentColor = Color(0xFF8FE3C4),
            containerColor = Color(0xFF1E4C3C),
            surfaceColor = surfaceColor,
            onSurfaceColor = onSurfaceColor,
            onClick = { fileLauncher.launch(arrayOf("audio/*")) },
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(Modifier.weight(1f))
      }
    }
  }
}

@Composable
@Preview
private fun SelectFolderPreview() {
  SelectFolder(
    onBack = {},
    onAdd = { _, _ -> },
    origin = Origin.Default,
  )
}
