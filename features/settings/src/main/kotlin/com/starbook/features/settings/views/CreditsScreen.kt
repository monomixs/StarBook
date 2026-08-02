package com.starbook.features.settings.views

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import com.starbook.core.common.AppInfoProvider
import com.starbook.core.common.rootGraphAs
import com.starbook.core.ui.icons.StarBookIcons
import com.starbook.navigation.Destination
import com.starbook.navigation.NavEntryProvider
import com.starbook.navigation.Navigator

@ContributesTo(AppScope::class)
interface CreditsGraph {
  val creditsViewModel: CreditsViewModel
}

@Inject
class CreditsViewModel(
  private val navigator: Navigator,
  appInfoProvider: AppInfoProvider
) {
  val appVersion: String = appInfoProvider.versionName

  fun openUrl(url: String) {
    navigator.goTo(Destination.Website(url))
  }

  fun back() {
    navigator.goBack()
  }
}

@ContributesTo(AppScope::class)
interface CreditsProvider {
  @Provides
  @IntoSet
  fun creditsNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.Credits> { key ->
    NavEntry(key) {
      CreditsScreen()
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsScreen() {
  val viewModel = androidx.compose.runtime.retain.retain { rootGraphAs<CreditsGraph>().creditsViewModel }

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = { Text("Credits", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = viewModel::back) {
            Icon(imageVector = StarBookIcons.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
      )
    },
    containerColor = MaterialTheme.colorScheme.background
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      contentPadding = PaddingValues(24.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      item {
        AppInfoCard(viewModel.appVersion)
      }

      item {
        CreditSection(
          title = "Original App",
          description = "StarBook is a forked repository of Voice, an excellent open-source audiobook player.",
          author = "Paul Woitaschek",
          url = "https://github.com/PaulWoitaschek/Voice",
          onUrlClick = { viewModel.openUrl(it) },
          accentColor = Color(0xFFB7C4FF)
        )
      }

      item {
        CreditSection(
          title = "UI Inspiration",
          description = "The player design and animations are inspired by PixelPlayer, a beautiful music player.",
          author = "PixelPlayerHQ",
          url = "https://github.com/PixelPlayerHQ/PixelPlayer",
          onUrlClick = { viewModel.openUrl(it) },
          accentColor = Color(0xFF8FE3C4)
        )
      }

      item {
          Spacer(Modifier.height(40.dp))
          Text(
              text = "Made with ❤️ for audiobook lovers.",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
          )
      }
    }
  }
}

@Composable
private fun AppInfoCard(version: String) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(32.dp),
    color = MaterialTheme.colorScheme.primaryContainer,
    tonalElevation = 4.dp
  ) {
    Column(
      modifier = Modifier.padding(28.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(80.dp)
          .clip(CircleShape)
          .background(
            Brush.linearGradient(
              listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = StarBookIcons.Star,
          contentDescription = null,
          modifier = Modifier.size(48.dp),
          tint = MaterialTheme.colorScheme.onPrimary
        )
      }

      Spacer(Modifier.height(20.dp))

      Text(
        text = "StarBook",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onPrimaryContainer
      )

      Text(
        text = "Version $version",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
      )

      Spacer(Modifier.height(16.dp))

      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
      ) {
          Text(
              text = "Maintained by Wedley",
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
          )
      }
    }
  }
}

@Composable
private fun CreditSection(
  title: String,
  description: String,
  author: String,
  url: String,
  onUrlClick: (String) -> Unit,
  accentColor: Color
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(28.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
      .border(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(28.dp)
      )
      .clickable { onUrlClick(url) }
      .padding(24.dp)
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = accentColor
    )

    Spacer(Modifier.height(8.dp))

    Text(
      text = description,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(Modifier.height(16.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = StarBookIcons.Person,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = author,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.weight(1f))
        Icon(
            imageVector = StarBookIcons.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
  }
}
