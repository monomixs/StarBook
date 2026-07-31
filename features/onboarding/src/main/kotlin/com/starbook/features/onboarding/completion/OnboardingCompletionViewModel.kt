package com.starbook.features.onboarding.completion

import androidx.datastore.core.DataStore
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import com.starbook.core.data.store.OnboardingCompletedStore
import com.starbook.navigation.Destination
import com.starbook.navigation.Navigator

@Inject
class OnboardingCompletionViewModel(
  @OnboardingCompletedStore
  private val onboardingCompletedStore: DataStore<Boolean>,
  private val navigator: Navigator,
) {

  private val scope = MainScope()

  fun next() {
    scope.launch {
      onboardingCompletedStore.updateData { true }
    }
    navigator.setRoot(Destination.BookOverview)
  }

  fun back() {
    navigator.goBack()
  }
}

