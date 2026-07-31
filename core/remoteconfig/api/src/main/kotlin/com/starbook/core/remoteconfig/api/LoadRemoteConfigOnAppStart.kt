package com.starbook.core.remoteconfig.api

import android.app.Application
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import kotlinx.coroutines.launch
import com.starbook.core.common.DispatcherProvider
import com.starbook.core.common.MainScope
import com.starbook.core.initializer.AppInitializer

@ContributesIntoSet(AppScope::class)
class LoadRemoteConfigOnAppStart(
  private val remoteConfig: RemoteConfig,
  dispatcherProvider: DispatcherProvider,
) : AppInitializer {

  private val mainScope = MainScope(dispatcherProvider)

  override fun onAppStart(application: Application) {
    mainScope.launch {
      remoteConfig.refresh()
    }
  }
}

