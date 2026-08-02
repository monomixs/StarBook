package com.starbook.core.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class ThemeColorScheme {
  @SerialName("StarBookBlue")
  StarBookBlue,

  @SerialName("Dynamic")
  Dynamic,

  @SerialName("Crimson")
  Crimson,

  @SerialName("Midnight")
  Midnight,

  @SerialName("Golden")
  Golden,

  @SerialName("Cyan")
  Cyan,

  @SerialName("Lime")
  Lime,
}

