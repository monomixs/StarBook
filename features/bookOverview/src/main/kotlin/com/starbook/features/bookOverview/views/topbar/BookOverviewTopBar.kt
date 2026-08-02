package com.starbook.features.bookOverview.views.topbar

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.starbook.core.data.BookId
import com.starbook.core.ui.StarBookTheme
import com.starbook.features.bookOverview.overview.BookOverviewLayoutMode
import com.starbook.features.bookOverview.overview.BookOverviewViewState
import com.starbook.features.bookOverview.search.BookSearchViewState
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun BookOverviewTopBar(
  viewState: BookOverviewViewState,
  onBookFolderClick: () -> Unit,
  onSettingsClick: () -> Unit,
  onActiveChange: (Boolean) -> Unit,
  onQueryChange: (String) -> Unit,
  onSearchBookClick: (BookId) -> Unit,
  onSearchBookLongClick: (BookId) -> Unit,
) {
  Column {
    val horizontalPadding by animateDpAsState(
      targetValue = if (viewState.searchActive) 0.dp else 16.dp,
      label = "horizontalPadding",
    )
    BookOverviewSearchBar(
      horizontalPadding = horizontalPadding,
      onQueryChange = onQueryChange,
      onActiveChange = onActiveChange,
      onBookFolderClick = onBookFolderClick,
      onSettingsClick = onSettingsClick,
      onSearchBookClick = onSearchBookClick,
      onSearchBookLongClick = onSearchBookLongClick,
      searchActive = viewState.searchActive,
      showAddBookHint = viewState.showAddBookHint,
      showFolderPickerIcon = viewState.showFolderPickerIcon,
      searchViewState = viewState.searchViewState,
    )
    var showLoading by remember { mutableStateOf(false) }
    LaunchedEffect(viewState.isLoading) {
      if (viewState.isLoading) {
        delay(3.seconds)
      }
      showLoading = viewState.isLoading
    }
    if (showLoading) {
      LinearProgressIndicator(
        Modifier
          .padding(top = 12.dp)
          .fillMaxWidth(),
      )
    }
  }
}

@Composable
@Preview
private fun BookOverviewTopBarPreview() {
  StarBookTheme {
    BookOverviewTopBar(
      viewState = BookOverviewViewState(
        books = emptyMap(),
        layoutMode = BookOverviewLayoutMode.List,
        playButtonState = BookOverviewViewState.PlayButtonState.Paused,
        showAddBookHint = true,
        showSearchIcon = true,
        isLoading = true,
        searchActive = true,
        searchViewState = BookSearchViewState.EmptySearch(
          books = listOf(),
          suggestedAuthors = listOf(),
          recentQueries = listOf(),
          query = "",
        ),
        showStoragePermissionBugCard = false,
        showFolderPickerIcon = true,
        dialog = null,
        selectedTab = com.starbook.navigation.Destination.Tab.LIBRARY,
      ),
      onBookFolderClick = {},
      onSettingsClick = {},
      onActiveChange = {},
      onQueryChange = {},
      onSearchBookClick = {},
      onSearchBookLongClick = {},
    )
  }
}

