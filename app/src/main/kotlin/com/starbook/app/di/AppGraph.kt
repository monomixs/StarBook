package com.starbook.app.di

import com.starbook.app.features.widget.BaseWidgetProvider
import com.starbook.features.widget.WidgetGraph

interface AppGraph : WidgetGraph {

  fun inject(target: App)
  override fun inject(target: BaseWidgetProvider)
}

