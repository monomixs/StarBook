package com.starbook.features.bookOverview.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.starbook.core.common.DispatcherProvider
import com.starbook.core.common.MainScope
import com.starbook.core.common.comparator.sortedNaturally
import com.starbook.core.data.BookId
import com.starbook.core.data.repo.BookRepository
import com.starbook.core.data.repo.internals.dao.RecentBookSearchDao
import com.starbook.core.playback.PlayerController
import com.starbook.core.search.BookSearch
import com.starbook.features.bookOverview.di.BookOverviewScope
import com.starbook.features.bookOverview.overview.BookOverviewLayoutMode
import com.starbook.features.bookOverview.overview.toItemViewState
import com.starbook.navigation.Navigator
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@SingleIn(BookOverviewScope::class)
@Inject
class BookSearchViewModel(
  private val repo: BookRepository,
  private val navigator: Navigator,
  private val recentBookSearchDao: RecentBookSearchDao,
  private val search: BookSearch,
  private val playerController: PlayerController,
  dispatcherProvider: DispatcherProvider,
) {

  private val scope = MainScope(dispatcherProvider)
  var query by mutableStateOf("")
    private set

  val state: StateFlow<BookSearchViewState> = combine(
    snapshotFlow { query },
    recentBookSearchDao.recentBookSearches().map { it.reversed() },
    repo.flow(),
    snapshotFlow { query }.flatMapLatest { q ->
      flow {
        emit(search.search(q))
      }
    },
  ) { query, recentBookSearches, allBooks, searchResults ->
    if (query.isNotBlank()) {
      BookSearchViewState.SearchResults(
        query = query,
        books = searchResults.map { it.toItemViewState() },
        layoutMode = BookOverviewLayoutMode.List,
      )
    } else {
      val suggestedAuthors = allBooks
        .mapNotNull { it.content.author }
        .toSet()
        .sortedNaturally()
      BookSearchViewState.EmptySearch(
        books = allBooks.map { it.toItemViewState() },
        recentQueries = recentBookSearches,
        suggestedAuthors = suggestedAuthors,
        query = query,
      )
    }
  }.stateIn(
    scope = scope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = BookSearchViewState.EmptySearch(
      books = emptyList(),
      suggestedAuthors = emptyList(),
      recentQueries = emptyList(),
      query = "",
    ),
  )

  fun onQueryChange(query: String) {
    this.query = query
  }

  fun onBookClick(id: BookId) {
    val query = query.trim()
    if (query.isNotBlank()) {
      scope.launch {
        recentBookSearchDao.add(query)
      }
    }
    // Optimization: Directly trigger playback from search screen to avoid full sheet opening
    playerController.play(id)
  }
}
