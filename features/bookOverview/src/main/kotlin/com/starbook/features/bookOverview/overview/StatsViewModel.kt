package com.starbook.features.bookOverview.overview

import com.starbook.core.data.repo.BookRepository
import com.starbook.features.bookOverview.di.BookOverviewScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.starbook.core.common.DispatcherProvider
import com.starbook.core.common.MainScope
import com.starbook.features.bookOverview.overview.BookOverviewCategory
import com.starbook.core.data.Book

@SingleIn(BookOverviewScope::class)
@Inject
class StatsViewModel(
  repo: BookRepository,
  dispatcherProvider: DispatcherProvider,
) {

  private val scope = MainScope(dispatcherProvider)

  data class StatsViewState(
    val totalHours: Int,
    val totalMinutes: Int,
    val bookCount: Int,
    val finishedCount: Int,
    val topByHours: List<BookStat>,
    val topAuthors: List<AuthorStat>,
    val inProgressBooks: List<BookStat>
  )

  data class BookStat(
    val id: String,
    val title: String,
    val author: String,
    val listenedHours: Int,
    val listenedMinutes: Int,
    val totalHours: Float,
    val progress: Float,
    val coverUrl: String?,
  )

  data class AuthorStat(
    val name: String,
    val bookCount: Int,
    val listenedHours: Int,
    val listenedMinutes: Int,
  )

  val state: StateFlow<StatsViewState> = repo.flow().map { books ->
    val finished = books.filter { it.category == BookOverviewCategory.FINISHED }
    val totalListenedMs = books.sumOf { it.content.totalTimeListenedMs }
    val totalHours = (totalListenedMs / (1000 * 60 * 60)).toInt()
    val totalMinutes = ((totalListenedMs / (1000 * 60)) % 60).toInt()

    val topByHours = books
      .sortedByDescending { it.content.totalTimeListenedMs }
      .take(5)
      .map { it.toBookStat() }

    val topAuthors = books
      .filter { it.content.author != null }
      .groupBy { it.content.author!! }
      .map { (name, authorBooks) ->
        val authorListenedMs = authorBooks.sumOf { it.content.totalTimeListenedMs }
        AuthorStat(
          name = name,
          bookCount = authorBooks.size,
          listenedHours = (authorListenedMs / (1000 * 60 * 60)).toInt(),
          listenedMinutes = ((authorListenedMs / (1000 * 60)) % 60).toInt()
        )
      }
      .sortedByDescending { it.listenedHours * 60 + it.listenedMinutes }
      .take(5)

    val inProgress = books
      .filter { it.category == BookOverviewCategory.CURRENT }
      .sortedByDescending { it.content.lastPlayedAt }
      .map { it.toBookStat() }

    StatsViewState(
      totalHours = totalHours,
      totalMinutes = totalMinutes,
      bookCount = books.size,
      finishedCount = finished.size,
      topByHours = topByHours,
      topAuthors = topAuthors,
      inProgressBooks = inProgress
    )
  }.stateIn(
    scope = scope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = StatsViewState(0, 0, 0, 0, emptyList(), emptyList(), emptyList())
  )

  private fun Book.toBookStat() = BookStat(
    id = id.value,
    title = content.name,
    author = content.author ?: "Unknown",
    listenedHours = (content.totalTimeListenedMs / (1000 * 60 * 60)).toInt(),
    listenedMinutes = ((content.totalTimeListenedMs / (1000 * 60)) % 60).toInt(),
    totalHours = duration.toFloat() / (1000 * 60 * 60),
    progress = if (duration > 0) position.toFloat() / duration.toFloat() else 0f,
    coverUrl = content.coverUrl
  )
}
