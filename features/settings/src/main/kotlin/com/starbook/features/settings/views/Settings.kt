package com.starbook.features.settings.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import com.starbook.core.common.rootGraphAs
import com.starbook.core.ui.StarBookTheme
import com.starbook.core.ui.icons.StarBookIcons
import com.starbook.features.settings.SettingsListener
import com.starbook.features.settings.SettingsViewEffect
import com.starbook.features.settings.SettingsViewModel
import com.starbook.features.settings.SettingsViewState
import com.starbook.features.settings.views.sleeptimer.AutoSleepTimerCard
import com.starbook.navigation.Destination
import com.starbook.navigation.NavEntryProvider
import com.starbook.core.strings.R as StringsR

@Composable
@Preview
private fun SettingsPreview() {
  StarBookTheme {
    Settings(
      SettingsViewState.preview(),
      SettingsListener.noop(),
    )
  }
}

@Composable
private fun Settings(
  viewState: SettingsViewState,
  listener: SettingsListener,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    snackbarHost = {
      SnackbarHost(hostState = snackbarHostState)
    },
    topBar = {
      TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
          Text(stringResource(StringsR.string.settings_action_open))
        },
        navigationIcon = {
          IconButton(
            onClick = {
              listener.close()
            },
          ) {
            Icon(
              imageVector = StarBookIcons.Close,
              contentDescription = stringResource(StringsR.string.common_action_close),
            )
          }
        },
      )
    },
  ) { contentPadding ->
    LazyColumn(contentPadding = contentPadding) {
      if (viewState.showDeveloperMenu && !viewState.kioskMode) {
        item {
          DeveloperMenuItem(
            onClick = listener::openDeveloperMenu,
          )
        }
      }
      item {
        ListItem(
          modifier = Modifier.clickable { listener.openFolderPicker() },
          leadingContent = {
            Icon(
              imageVector = StarBookIcons.Book,
              contentDescription = stringResource(StringsR.string.library_folders_title),
            )
          },
          headlineContent = {
            Text(stringResource(StringsR.string.library_folders_title))
          },
          supportingContent = {
            Text(stringResource(StringsR.string.settings_library_folders_summary))
          },
        )
      }
      item {
        ThemeModeRow(viewState.themeMode, listener::onThemeModeRowClick)
      }
      if (viewState.showThemeColorSchemePref) {
        item {
          ThemeColorSchemeRow(viewState.themeColorScheme, listener::onThemeColorSchemeRowClick)
        }
      }
      if (viewState.showAnalyticSetting && !viewState.kioskMode) {
        item {
          AnalyticsRow(analyticsEnabled = viewState.analyticsEnabled, toggle = listener::toggleAnalytics)
        }
      }
      item {
        ListItem(
          modifier = Modifier.clickable { listener.toggleGrid() },
          leadingContent = {
            val icon = if (viewState.useGrid) {
              StarBookIcons.GridView
            } else {
              StarBookIcons.ViewList
            }
            Icon(
              imageVector = icon,
              contentDescription = stringResource(StringsR.string.settings_library_use_grid_title),
            )
          },
          headlineContent = { Text(stringResource(StringsR.string.settings_library_use_grid_title)) },
          trailingContent = {
            Switch(
              checked = viewState.useGrid,
              onCheckedChange = {
                listener.toggleGrid()
              },
            )
          },
        )
      }

      item {
        SeekTimeRow(viewState.seekTimeInSeconds) {
          listener.onSeekAmountRowClick()
        }
      }

      item {
        AutoRewindRow(viewState.autoRewindInSeconds) {
          listener.onAutoRewindRowClick()
        }
      }

      item {
        AutoSleepTimerCard(viewState.autoSleepTimer, listener)
      }

      item {
        AppVersion(
          appVersion = viewState.appVersion,
          onClick = listener::onAppVersionClick,
        )
      }

      item {
        ListItem(
          modifier = Modifier.clickable { listener.openCredits() },
          leadingContent = {
            Icon(
              imageVector = StarBookIcons.Help,
              contentDescription = null,
            )
          },
          headlineContent = {
            Text("About")
          },
          supportingContent = {
            Text("Credits and information")
          },
        )
      }
      if (viewState.kioskMode) {
        if (viewState.showAnalyticSetting) {
          item {
            AnalyticsRow(analyticsEnabled = viewState.analyticsEnabled, toggle = listener::toggleAnalytics)
          }
        }
        if (viewState.showDeveloperMenu) {
          item {
            DeveloperMenuItem(
              onClick = listener::openDeveloperMenu,
            )
          }
        }
      }
    }
    Dialog(viewState, listener)
  }
}

@Composable
private fun AnalyticsRow(
  analyticsEnabled: Boolean,
  toggle: () -> Unit,
) {
  ListItem(
    modifier = Modifier.clickable { toggle() },
    leadingContent = {
      Icon(
        imageVector = StarBookIcons.Analytics,
        contentDescription = null,
      )
    },
    headlineContent = {
      Text(text = stringResource(StringsR.string.settings_analytics_consent_title))
    },
    supportingContent = {
      Text(text = stringResource(StringsR.string.settings_analytics_consent_description))
    },
    trailingContent = {
      Switch(
        checked = analyticsEnabled,
        onCheckedChange = { toggle() },
      )
    },
  )
}

@ContributesTo(AppScope::class)
interface SettingsGraph {
  val settingsViewModel: SettingsViewModel
}

@ContributesTo(AppScope::class)
interface SettingsProvider {

  @Provides
  @IntoSet
  fun settingsNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.Settings> { key ->
    NavEntry(key) {
      Settings()
    }
  }
}

@Composable
fun Settings() {
  val viewModel = retain<SettingsViewModel> { rootGraphAs<SettingsGraph>().settingsViewModel }
  val snackbarHostState = remember { SnackbarHostState() }
  val viewState = viewModel.viewState()
  val currentDeveloperMenuUnlockedMessage = rememberUpdatedState("Developer Menu unlocked")
  LaunchedEffect(viewModel) {
    viewModel.viewEffects.collect { viewEffect ->
      when (viewEffect) {
        SettingsViewEffect.DeveloperMenuUnlocked -> {
          snackbarHostState.showSnackbar(currentDeveloperMenuUnlockedMessage.value)
        }
      }
    }
  }
  Settings(viewState, viewModel, snackbarHostState)
}

@Composable
private fun Dialog(
  viewState: SettingsViewState,
  listener: SettingsListener,
) {
  val dialog = viewState.dialog ?: return
  when (dialog) {
    SettingsViewState.Dialog.AutoRewindAmount -> {
      AutoRewindAmountDialog(
        currentSeconds = viewState.autoRewindInSeconds,
        onSecondsConfirm = listener::autoRewindAmountChang,
        onDismiss = listener::dismissDialog,
      )
    }
    SettingsViewState.Dialog.SeekTime -> {
      SeekAmountDialog(
        currentSeconds = viewState.seekTimeInSeconds,
        onSecondsConfirm = listener::seekAmountChanged,
        onDismiss = listener::dismissDialog,
      )
    }
    SettingsViewState.Dialog.Theme -> {
      ThemeModeDialog(
        selectedThemeMode = viewState.themeMode,
        onThemeModeSelect = listener::setThemeMode,
        onDismiss = listener::dismissDialog,
      )
    }
    SettingsViewState.Dialog.ColorScheme -> {
      ThemeColorSchemeDialog(
        selectedThemeColorScheme = viewState.themeColorScheme,
        onThemeColorSchemeSelect = listener::setThemeColorScheme,
        onDismiss = listener::dismissDialog,
      )
    }
  }
}

