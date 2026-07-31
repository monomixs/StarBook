package com.starbook.app.di

import android.app.Application
import dev.zacsweers.metro.HasMemberInjections
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.createGraphFactory
import com.starbook.core.common.rootGraph
import com.starbook.core.initializer.AppInitializer

@HasMemberInjections
open class App : Application() {

  @Inject
  lateinit var appInitializers: Set<AppInitializer>

  override fun onCreate() {
    super.onCreate()

    rootGraph = createGraph()
      .also { graph ->
        graph.inject(this)
      }

    appInitializers.forEach {
      it.onAppStart(this)
    }
  }

  open fun createGraph(): AppGraph {
    return createGraphFactory<ProductionAppGraph.Factory>().create(this)
  }
}

