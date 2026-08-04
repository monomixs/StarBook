package com.starbook.core.ui

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@SingleIn(AppScope::class)
@Inject
public class GlobalLoadingState {
    private val _isShowing = MutableStateFlow(false)
    public val isShowing: StateFlow<Boolean> = _isShowing

    public fun show() {
        _isShowing.value = true
    }

    public fun hide() {
        _isShowing.value = false
    }
}
