package com.starbook.features.bookOverview.overview

import androidx.compose.runtime.Immutable
import com.starbook.core.data.Book
import com.starbook.core.data.BookId
import com.starbook.core.logging.api.Logger
import com.starbook.core.ui.formatTime

@Immutable
data class BookOverviewItemViewState(
  val name: String,
  val author: String?,
  val cover: String?,
  val progress: Float,
  val id: BookId,
  val remainingTime: String,
  val genre: String? = null,
  val duration: String? = null,
  val category: BookOverviewCategory = BookOverviewCategory.NOT_STARTED,
)

internal fun Book.toItemViewState() = BookOverviewItemViewState(
  name = content.name,
  author = content.author,
  cover = content.coverUrl,
  id = id,
  progress = progress(),
  remainingTime = formatTime(duration - position),
  genre = content.genre,
  duration = formatTime(duration),
  category = category,
)

private fun Book.progress(): Float {
  val globalPosition = position
  val totalDuration = duration
  if (totalDuration == 0L) return 0F
  val progress = globalPosition.toFloat() / totalDuration.toFloat()
  if (progress < 0F) {
    Logger.w("Couldn't determine progress for book=$this")
  }
  return progress.coerceIn(0F, 1F)
}

