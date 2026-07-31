package com.starbook.features.onboarding.welcome

import android.net.Uri
import androidx.datastore.core.DataStore
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import com.starbook.core.data.folders.AudiobookFolders
import com.starbook.core.data.folders.FolderType
import com.starbook.core.data.store.OnboardingCompletedStore
import com.starbook.navigation.Destination
import com.starbook.navigation.Navigator
import com.starbook.navigation.Origin

@Inject
class OnboardingWelcomeViewModel(
  private val navigator: Navigator,
  private val audiobookFolders: AudiobookFolders,
  @OnboardingCompletedStore
  private val onboardingCompletedStore: DataStore<Boolean>,
) {

  private val scope = MainScope()

  fun onFilePicked(uri: Uri) {
    audiobookFolders.add(uri, FolderType.SingleFile)
  }

  fun onFolderPicked(uri: Uri) {
    navigator.goTo(
      Destination.SelectFolderType(
        uri = uri,
        origin = Origin.Onboarding,
      ),
    )
  }

  fun startListening() {
    scope.launch {
      onboardingCompletedStore.updateData { true }
      navigator.setRoot(Destination.BookOverview)
    }
  }
}
