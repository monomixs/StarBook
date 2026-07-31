package com.starbook.features.onboarding.completion

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import com.starbook.features.onboarding.UnifiedOnboardingScreen
import com.starbook.navigation.Destination
import com.starbook.navigation.NavEntryProvider

@ContributesTo(AppScope::class)
interface OnboardingCompletionProvider {

  @Provides
  @IntoSet
  fun onboardingCompletionNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.OnboardingCompletion> { key ->
    NavEntry(key) {
      UnifiedOnboardingScreen(initialStep = 4)
    }
  }
}

@Composable
fun OnboardingCompletion(modifier: Modifier = Modifier) {
  UnifiedOnboardingScreen(initialStep = 4, modifier = modifier)
}
