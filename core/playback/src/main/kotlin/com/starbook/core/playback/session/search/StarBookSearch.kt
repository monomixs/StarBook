package com.starbook.core.playback.session.search

data class StarBookSearch(
  val query: String? = null,
  val mediaFocus: String? = null,
  val album: String? = null,
  val artist: String? = null,
)

