package com.starbook.core.scanner.matroska

import com.starbook.core.data.MarkData

internal data class MatroskaMediaInfo(
  val album: String? = null,
  val artist: String? = null,
  val title: String? = null,
  val chapters: List<MarkData> = emptyList(),
)

