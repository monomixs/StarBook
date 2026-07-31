package com.starbook.features.support

import kotlinx.coroutines.flow.StateFlow

interface SupportBackend {
  val state: StateFlow<SupportBackendState>

  fun openSupport()
}

