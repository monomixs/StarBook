package com.starbook.core.analytics.noop

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import com.starbook.core.analytics.api.Analytics
import com.starbook.core.logging.api.Logger

@ContributesBinding(AppScope::class)
class NoOpAnalytics : Analytics {

  override fun screenView(screenName: String) {
    Logger.v("screenView($screenName)")
  }

  override fun event(
    name: String,
    params: Map<String, String>,
  ) {
    Logger.v("event(name=$name, params=$params)")
  }
}

