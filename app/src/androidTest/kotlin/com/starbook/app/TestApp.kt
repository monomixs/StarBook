package com.starbook.app

import dev.zacsweers.metro.createGraphFactory
import com.starbook.app.di.App
import com.starbook.app.di.AppGraph

class TestApp : App() {

  override fun createGraph(): AppGraph {
    return createGraphFactory<TestGraph.Factory>()
      .create(this)
  }
}

