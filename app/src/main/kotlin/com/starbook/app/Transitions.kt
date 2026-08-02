package com.starbook.app

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.get
import androidx.navigation3.scene.Scene
import com.starbook.app.navigation.DestinationMetadataKey
import com.starbook.navigation.Destination

private val SpringSpec = spring<Float>(
  dampingRatio = 0.8f,
  stiffness = Spring.StiffnessMediumLow
)

private val SlideSpringSpec = spring<IntOffset>(
  dampingRatio = 0.8f,
  stiffness = Spring.StiffnessMediumLow
)

/**
 * Screen switching animation matching slide.html - Optimized for Android
 */
val StarBookEnterTransition: (forward: Boolean) -> EnterTransition = { forward ->
  slideInHorizontally(
    animationSpec = SlideSpringSpec,
    initialOffsetX = { if (forward) it else -it },
  ) + scaleIn(
    initialScale = 0.94f,
    animationSpec = SpringSpec
  ) + fadeIn(
    initialAlpha = 0.4f,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
  )
}

/**
 * Screen switching animation matching slide.html - Optimized for Android
 */
val StarBookExitTransition: (forward: Boolean) -> ExitTransition = { forward ->
  slideOutHorizontally(
    animationSpec = SlideSpringSpec,
    targetOffsetX = { if (forward) -it else it },
  ) + scaleOut(
    targetScale = 0.94f,
    animationSpec = SpringSpec
  ) + fadeOut(
    targetAlpha = 0.4f,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
  )
}

internal fun Scene<Destination.Compose>.destination(): Destination.Compose? {
  return entries.lastOrNull()?.metadata?.get(DestinationMetadataKey)
}

internal fun isBookOverviewPlaybackTransition(
  initial: Destination.Compose?,
  target: Destination.Compose?,
): Boolean {
  return (initial == Destination.BookOverview && target is Destination.Playback) ||
    (initial is Destination.Playback && target == Destination.BookOverview)
}
