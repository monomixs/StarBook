package com.starbook.features.bookOverview.search

import androidx.compose.runtime.Immutable
import com.starbook.features.bookOverview.overview.BookOverviewItemViewState
import com.starbook.features.bookOverview.overview.BookOverviewLayoutMode

@Immutable
sealed interface BookSearchViewState {
  val query: String

  data class SearchResults(
    val books: List<BookOverviewItemViewState>,
    val layoutMode: BookOverviewLayoutMode,
    override val query: String,
  ) : BookSearchViewState

  data class EmptySearch(
    val books: List<BookOverviewItemViewState>,
    val suggestedAuthors: List<String>,
    val recentQueries: List<String>,
    override val query: String,
  ) : BookSearchViewState
}

