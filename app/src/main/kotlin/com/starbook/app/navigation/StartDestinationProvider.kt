package com.starbook.app.navigation

import android.content.Intent
import androidx.datastore.core.DataStore
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import com.starbook.app.MainActivity
import com.starbook.core.data.BookId
import com.starbook.core.data.folders.AudiobookFolders
import com.starbook.core.data.store.CurrentBookStore
import com.starbook.core.data.store.OnboardingCompletedStore
import com.starbook.core.playback.PlayerController
import com.starbook.navigation.Destination

@Inject
class StartDestinationProvider(
  @OnboardingCompletedStore
  private val onboardingCompletedStore: DataStore<Boolean>,
  private val audiobookFolders: AudiobookFolders,
  @CurrentBookStore
  private val currentBookStore: DataStore<BookId?>,
  private val playerController: PlayerController,
) {

  operator fun invoke(intent: Intent): List<Destination.Compose> {
    val showOnboarding = runBlocking { showOnboarding() }
    if (showOnboarding) {
      return listOf(Destination.OnboardingWelcome)
    }

    val goToBook = intent.getBooleanExtra(MainActivity.NI_GO_TO_BOOK, false)
    if (goToBook) {
      val bookId = runBlocking { currentBookStore.data.first() }
      if (bookId != null) {
        return listOf(Destination.Home, Destination.Playback(bookId))
      }
    }

    if (intent.action == "playCurrent") {
      val bookId = runBlocking { currentBookStore.data.first() }
      if (bookId != null) {
        playerController.play()
        return listOf(Destination.Home, Destination.Playback(bookId))
      }
    }
    return listOf(Destination.Home)
  }

  private suspend fun showOnboarding(): Boolean {
    return when {
      onboardingCompletedStore.data.first() -> false
      audiobookFolders.hasAnyFolders() -> false
      else -> true
    }
  }
}

