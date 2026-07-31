package com.starbook.features.onboarding.explanation

import androidx.datastore.core.DataStore
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import com.starbook.core.common.AppInfoProvider
import com.starbook.core.data.store.AnalyticsConsentStore
import com.starbook.navigation.Destination
import com.starbook.navigation.Navigator
import com.starbook.navigation.Origin

@Inject
class OnboardingExplanationViewModel(
  private val navigator: Navigator,
  @AnalyticsConsentStore
  private val analyticsConsentStore: DataStore<Boolean>,
  private val appInfoProvider: AppInfoProvider,
) {

  private val scope = MainScope()

  fun viewState(): OnboardingExplanationViewState {
    return OnboardingExplanationViewState(
      askForAnalytics = appInfoProvider.analyticsIncluded,
    )
  }

  fun onContinueWithAnalytics() {
    scope.launch {
      analyticsConsentStore.updateData { true }
    }
    navigator.goTo(Destination.AddContent(origin = Origin.Onboarding))
  }

  fun onContinueWithoutAnalytics() {
    scope.launch {
      analyticsConsentStore.updateData { false }
    }
    navigator.goTo(Destination.AddContent(origin = Origin.Onboarding))
  }

  fun onPrivacyPolicyClick() {
    navigator.goTo(Destination.Website("https://starbook.com/privacy-policy"))
  }

  fun onClose() {
    navigator.goBack()
  }
}

