package com.starbook.core.logging.debug

import android.app.Application
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import com.starbook.core.initializer.AppInitializer
import com.starbook.core.logging.api.Logger

@ContributesIntoSet(AppScope::class)
class DebugLogWriterInitializer : AppInitializer {

  override fun onAppStart(application: Application) {
    Logger.install(DebugLogWriter())
  }
}

