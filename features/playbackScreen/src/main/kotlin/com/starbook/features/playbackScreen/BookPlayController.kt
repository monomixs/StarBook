package com.starbook.features.playbackScreen

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavEntry
import com.starbook.core.common.rootGraphAs
import com.starbook.core.data.BookId
import com.starbook.features.playbackScreen.pixelplayer.components.SelectChapterDialog
import com.starbook.features.playbackScreen.pixelplayer.components.SpeedDialog
import com.starbook.features.playbackScreen.pixelplayer.components.VolumeGainDialog
import com.starbook.features.playbackScreen.view.BookPlayView
import com.starbook.features.sleepTimer.SleepTimerDialog
import com.starbook.navigation.Destination
import com.starbook.navigation.NavEntryProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import com.starbook.core.strings.R as StringsR

@Composable
fun BookPlayScreen(bookId: BookId) {
  val viewModel = retain(bookId.value) {
    rootGraphAs<BookPlayGraph>()
      .bookPlayViewModelFactory
      .create(bookId)
  }
  val snackbarHostState = remember { SnackbarHostState() }
  val dialogState = viewModel.dialogState.value
  val viewState by viewModel.state.collectAsState()

  if (viewState == null) return

  val bookmarkAddedMessage = stringResource(StringsR.string.bookmark_added_snackbar)
  val batteryOptimizationMessage = stringResource(StringsR.string.playback_battery_optimization_rationale)
  val batteryOptimizationAction = stringResource(StringsR.string.playback_battery_optimization_action)
  LaunchedEffect(viewModel) {
    viewModel.viewEffects.collect { viewEffect ->
      when (viewEffect) {
        BookPlayViewEffect.BookmarkAdded -> {
          snackbarHostState.showSnackbar(message = bookmarkAddedMessage)
        }
        BookPlayViewEffect.RequestIgnoreBatteryOptimization -> {
          val result = snackbarHostState.showSnackbar(
            message = batteryOptimizationMessage,
            duration = SnackbarDuration.Long,
            actionLabel = batteryOptimizationAction,
          )
          if (result == SnackbarResult.ActionPerformed) {
            viewModel.onBatteryOptimizationRequested()
          }
        }
      }
    }
  }
  BookPlayView(
    viewState!!,
    bookId = bookId,
    onPlayClick = viewModel::playPause,
    onFastForwardClick = viewModel::fastForward,
    onRewindClick = viewModel::rewind,
    onSeek = viewModel::seekTo,
    onBookmarkClick = viewModel::onBookmarkClick,
    onBookmarkLongClick = viewModel::onBookmarkLongClick,
    onSkipSilenceClick = viewModel::toggleSkipSilence,
    onSleepTimerClick = viewModel::toggleSleepTimer,
    onVolumeBoostClick = viewModel::onVolumeGainIconClick,
    onSpeedChangeClick = viewModel::onPlaybackSpeedIconClick,
    onCloseClick = viewModel::onCloseClick,
    onSkipToNext = viewModel::next,
    onSkipToPrevious = viewModel::previous,
    onCurrentChapterClick = viewModel::onCurrentChapterClick,
    useLandscapeLayout = LocalConfiguration.current.orientation == ORIENTATION_LANDSCAPE,
    snackbarHostState = snackbarHostState,
  )
  if (dialogState != null) {
    when (dialogState) {
      is BookPlayDialogViewState.SpeedDialog -> {
        SpeedDialog(dialogState, viewModel::onPlaybackSpeedChanged) {
          viewModel.dismissDialog()
        }
      }
      is BookPlayDialogViewState.VolumeGainDialog -> {
        VolumeGainDialog(dialogState, viewModel::onVolumeGainChanged) {
          viewModel.dismissDialog()
        }
      }
      is BookPlayDialogViewState.SelectChapterDialog -> {
        SelectChapterDialog(dialogState, viewModel::onChapterClick) {
          viewModel.dismissDialog()
        }
      }
      is BookPlayDialogViewState.SleepTimer -> {
        SleepTimerDialog(
          viewState = dialogState.viewState,
          onDismiss = viewModel::dismissDialog,
          onIncrementSleepTime = viewModel::incrementSleepTime,
          onDecrementSleepTime = viewModel::decrementSleepTime,
          onAcceptSleepTime = viewModel::onAcceptSleepTime,
          onAcceptSleepAtEndOfChapter = viewModel::onAcceptSleepAtEndOfChapter,
        )
      }
    }
  }
}

@ContributesTo(AppScope::class)
interface BookPlayGraph {
  val bookPlayViewModelFactory: BookPlayViewModel.Factory
}

@ContributesTo(AppScope::class)
interface BookPlayProvider {

  @Provides
  @IntoSet
  fun bookPlayNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.Playback> { key ->
    NavEntry(key) {
      // The player UI is now handled by UnifiedPlayerSheetV2 in MainActivity.
      // We render an empty box here with background to avoid white flash during transition.
      Box(
        Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.background)
      )
    }
  }
}
