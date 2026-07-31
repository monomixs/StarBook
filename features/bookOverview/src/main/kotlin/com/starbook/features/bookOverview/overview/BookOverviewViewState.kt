package com.starbook.features.bookOverview.overview

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import com.starbook.core.data.BookId
import com.starbook.features.bookOverview.search.BookSearchViewState

import com.starbook.navigation.Destination

@Immutable
data class BookOverviewViewState(
  val books: Map<BookOverviewCategory, Map<BookId, BookOverviewItemViewState>>,
  val layoutMode: BookOverviewLayoutMode,
  val playButtonState: PlayButtonState?,
  val showAddBookHint: Boolean,
  val showSearchIcon: Boolean,
  val isLoading: Boolean,
  val searchActive: Boolean,
  val searchViewState: BookSearchViewState,
  val showStoragePermissionBugCard: Boolean,
  val showFolderPickerIcon: Boolean,
  val dialog: Dialog?,
  val selectedTab: Destination.Tab,
) {

  companion object {
    val Loading = BookOverviewViewState(
      books = mapOf(),
      layoutMode = BookOverviewLayoutMode.List,
      playButtonState = null,
      showAddBookHint = false,
      showSearchIcon = false,
      isLoading = true,
      searchActive = false,
      searchViewState = BookSearchViewState.EmptySearch(
        books = emptyList(),
        suggestedAuthors = emptyList(),
        recentQueries = emptyList(),
        query = "",
      ),
      showStoragePermissionBugCard = false,
      showFolderPickerIcon = true,
      dialog = null,
      selectedTab = Destination.Tab.LIBRARY,
    )
  }

  enum class PlayButtonState {
    Playing,
    Paused,
  }

  enum class Dialog {
    FolderPickerMovedToSettings,
  }
}

