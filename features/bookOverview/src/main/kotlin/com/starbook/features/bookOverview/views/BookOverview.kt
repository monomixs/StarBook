package com.starbook.features.bookOverview.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.runtime.*
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import com.starbook.core.common.rootGraphAs
import com.starbook.core.data.BookId
import com.starbook.core.strings.R as StringsR
import com.starbook.core.ui.StarBookTheme
import com.starbook.core.ui.icons.StarBookIcons
import com.starbook.features.bookOverview.bottomSheet.AudiobookDetailsBottomSheet
import com.starbook.features.bookOverview.deleteBook.DeleteBookDialog
import com.starbook.features.bookOverview.di.BookOverviewGraph
import com.starbook.features.bookOverview.editTitle.EditBookTitleDialog
import com.starbook.features.bookOverview.metadata.MetadataEditorScreen
import com.starbook.features.bookOverview.overview.*
import com.starbook.features.bookOverview.search.BookSearchViewState
import com.starbook.features.bookOverview.views.topbar.BookOverviewTopBar
import com.starbook.navigation.Destination
import com.starbook.navigation.NavEntryProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlin.uuid.Uuid

@ContributesTo(AppScope::class)
interface BookOverviewProvider {

  @Provides
  @IntoSet
  fun bookOverviewNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.BookOverview> { key ->
    NavEntry(key) {
      BookOverviewScreen(Destination.Tab.LIBRARY)
    }
  }

  @Provides
  @IntoSet
  fun homeNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.Home> { key ->
    NavEntry(key) {
      BookOverviewScreen(Destination.Tab.HOME)
    }
  }

  @Provides
  @IntoSet
  fun searchNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.Search> { key ->
    NavEntry(key) {
      BookOverviewScreen(Destination.Tab.SEARCH)
    }
  }

  @Provides
  @IntoSet
  fun metadataEditorNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.MetadataEditor> { key ->
    NavEntry(key) {
      MetadataEditorScreen(
        bookId = key.bookId,
        viewModel = retain<BookOverviewGraph> {
          rootGraphAs<BookOverviewGraph.Factory.Provider>()
            .bookOverviewGraphProviderFactory.create()
        }.metadataEditorViewModel
      )
    }
  }
}

@Composable
fun BookOverviewScreen(
  tab: Destination.Tab,
  modifier: Modifier = Modifier,
) {
  val bookGraph = retain<BookOverviewGraph> {
    rootGraphAs<BookOverviewGraph.Factory.Provider>()
      .bookOverviewGraphProviderFactory.create()
  }
  val bookOverviewViewModel = bookGraph.bookOverviewViewModel
  val statsViewModel = bookGraph.statsViewModel
  val editBookTitleViewModel = bookGraph.editBookTitleViewModel
  val bottomSheetViewModel = bookGraph.bottomSheetViewModel
  val deleteBookViewModel = bookGraph.deleteBookViewModel

  LaunchedEffect(Unit) {
    bookOverviewViewModel.attach()
  }
  val viewState by bookOverviewViewModel.state.collectAsState()

  var showBottomSheet by remember { mutableStateOf(false) }

  when (tab) {
    Destination.Tab.HOME -> {
      val statsState by statsViewModel.state.collectAsState()
      HomeScreen(
        stats = statsState,
        onBookClick = { bookOverviewViewModel.onBookClick(BookId(it)) },
        onBookLongClick = { bookId ->
            bottomSheetViewModel.bookSelected(BookId(bookId))
            showBottomSheet = true
        },
        onSettingsClick = bookOverviewViewModel::onSettingsClick,
        onGoToLibraryClick = bookOverviewViewModel::onGoToLibrary
      )
    }
    Destination.Tab.SEARCH -> {
      val searchViewModel = bookGraph.bookSearchViewModel
      val searchViewState by searchViewModel.state.collectAsState()
      com.starbook.features.bookOverview.search.BookSearchScreen(
        viewState = searchViewState,
        query = searchViewModel.query,
        onQueryChange = searchViewModel::onQueryChange,
        onBookClick = searchViewModel::onBookClick,
        onBookLongClick = { bookId ->
            bottomSheetViewModel.bookSelected(bookId)
            showBottomSheet = true
        },
        onSettingsClick = bookOverviewViewModel::onSettingsClick
      )
    }
    Destination.Tab.LIBRARY -> {
      BookOverview(
        viewState = viewState,
        onSettingsClick = bookOverviewViewModel::onSettingsClick,
        onBookClick = bookOverviewViewModel::onBookClick,
        onBookLongClick = { bookId ->
          bottomSheetViewModel.bookSelected(bookId)
          showBottomSheet = true
        },
        onBookFolderClick = bookOverviewViewModel::onBookFolderClick,
        onFolderPickerMovedDialogDismiss = bookOverviewViewModel::onFolderPickerMovedDialogDismiss,
        onPlayButtonClick = bookOverviewViewModel::playPause,
        onSearchActiveChange = bookOverviewViewModel::onSearchActiveChange,
        onSearchQueryChange = bookOverviewViewModel::onSearchQueryChange,
        onSearchBookClick = bookOverviewViewModel::onSearchBookClick,
        onPermissionBugCardClick = bookOverviewViewModel::onPermissionBugCardClick,
        onTabClick = bookOverviewViewModel::onTabClick,
      )
    }
  }

  val deleteBookViewState = deleteBookViewModel.state.value
  if (deleteBookViewState != null) {
    DeleteBookDialog(
      viewState = deleteBookViewState,
      onDismiss = deleteBookViewModel::onDismiss,
      onConfirmDeletion = deleteBookViewModel::onConfirmDeletion,
      onDeleteCheckBoxCheck = deleteBookViewModel::onDeleteCheckBoxCheck,
    )
  }
  val editBookTitleState = editBookTitleViewModel.state.value
  if (editBookTitleState != null) {
    EditBookTitleDialog(
      onDismissEditTitleClick = editBookTitleViewModel::onDismissEditTitle,
      onConfirmEditTitle = editBookTitleViewModel::onConfirmEditTitle,
      viewState = editBookTitleState,
      onUpdateEditTitle = editBookTitleViewModel::onUpdateEditTitle,
    )
  }

  if (showBottomSheet) {
    val sheetState = rememberBottomSheetState(
      initialValue = Hidden,
      enabledValues = setOf(Hidden, Expanded),
    )
    ModalBottomSheet(
      modifier = modifier,
      sheetState = sheetState,
      content = {
        bottomSheetViewModel.book?.let { book ->
          AudiobookDetailsBottomSheet(
            book = book,
            sheetState = bottomSheetViewModel.sheetState,
            onResume = {
              bottomSheetViewModel.onResumeClick()
              showBottomSheet = false
            },
            onEdit = {
              bottomSheetViewModel.onEditClick()
              showBottomSheet = false
            },
            onItemClick = { item ->
              bottomSheetViewModel.onItemClick(item)
              showBottomSheet = false
            }
          )
        }
      },
      onDismissRequest = {
        showBottomSheet = false
      },
    )
  }
}

@Composable
internal fun BookOverview(
  viewState: BookOverviewViewState,
  onSettingsClick: () -> Unit,
  onBookClick: (BookId) -> Unit,
  onBookLongClick: (BookId) -> Unit,
  onBookFolderClick: () -> Unit,
  onFolderPickerMovedDialogDismiss: () -> Unit,
  onPlayButtonClick: () -> Unit,
  onSearchActiveChange: (Boolean) -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onSearchBookClick: (BookId) -> Unit,
  onPermissionBugCardClick: () -> Unit,
  onTabClick: (Destination.Tab) -> Unit,
  modifier: Modifier = Modifier,
) {
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  Scaffold(
    modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    containerColor = Color.Transparent,
    topBar = {
      BookOverviewTopBar(
        viewState = viewState,
        onBookFolderClick = onBookFolderClick,
        onSettingsClick = onSettingsClick,
        onActiveChange = onSearchActiveChange,
        onQueryChange = onSearchQueryChange,
        onSearchBookClick = onSearchBookClick,
        onSearchBookLongClick = onBookLongClick,
      )
    },
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
  ) { contentPadding ->
    Box(
      Modifier
        .padding(contentPadding)
        .consumeWindowInsets(contentPadding),
    ) {
      when (viewState.layoutMode) {
        BookOverviewLayoutMode.List -> {
          ListBooks(
            books = viewState.books,
            onBookClick = onBookClick,
            onBookLongClick = onBookLongClick,
            showPermissionBugCard = viewState.showStoragePermissionBugCard,
            onPermissionBugCardClick = onPermissionBugCardClick,
          )
        }
        BookOverviewLayoutMode.Grid -> {
          GridBooks(
            books = viewState.books,
            onBookClick = onBookClick,
            onBookLongClick = onBookLongClick,
            showPermissionBugCard = viewState.showStoragePermissionBugCard,
            onPermissionBugCardClick = onPermissionBugCardClick,
          )
        }
      }
    }
  }
  Dialog(
    dialog = viewState.dialog,
    onFolderPickerMovedDialogDismiss = onFolderPickerMovedDialogDismiss,
  )
}

@Composable
private fun Dialog(
  dialog: BookOverviewViewState.Dialog?,
  onFolderPickerMovedDialogDismiss: () -> Unit,
) {
  when (dialog) {
    BookOverviewViewState.Dialog.FolderPickerMovedToSettings -> {
      AlertDialog(
        onDismissRequest = onFolderPickerMovedDialogDismiss,
        icon = {
          Row {
            Icon(imageVector = StarBookIcons.ArrowForward, contentDescription = null)
            Icon(imageVector = StarBookIcons.Settings, contentDescription = null)
            Icon(imageVector = StarBookIcons.ArrowBack, contentDescription = null)
          }
        },
        title = {
          Text(stringResource(StringsR.string.library_folders_moved_dialog_title))
        },
        text = {
          Text(stringResource(StringsR.string.library_folders_moved_dialog_message))
        },
        confirmButton = {
          TextButton(onClick = onFolderPickerMovedDialogDismiss) {
            Text(stringResource(StringsR.string.common_dialog_ok))
          }
        },
      )
    }
    null -> Unit
  }
}

@Suppress("ktlint:compose:preview-public-check")
@Preview
@Composable
fun BookOverviewPreview(
  @PreviewParameter(BookOverviewPreviewParameterProvider::class)
  viewState: BookOverviewViewState,
) {
  StarBookTheme {
    BookOverview(
      viewState = viewState,
      onSettingsClick = {},
      onBookClick = {},
      onBookLongClick = {},
      onBookFolderClick = {},
      onFolderPickerMovedDialogDismiss = {},
      onPlayButtonClick = {},
      onSearchActiveChange = {},
      onSearchQueryChange = {},
      onSearchBookClick = {},
      onPermissionBugCardClick = {},
      onTabClick = {},
    )
  }
}

internal class BookOverviewPreviewParameterProvider : PreviewParameterProvider<BookOverviewViewState> {

  fun book(): BookOverviewItemViewState {
    return BookOverviewItemViewState(
      name = "Book",
      author = "Author",
      cover = null,
      progress = 0.8F,
      id = BookId(Uuid.random().toString()),
      remainingTime = "01:04",
      genre = "Genre",
      duration = "05:20",
      category = BookOverviewCategory.CURRENT,
    )
  }

  override val values = sequenceOf(
    BookOverviewViewState(
      books = mapOf(
        BookOverviewCategory.CURRENT to buildMap {
          repeat(10) {
            put(
              BookId(Uuid.random().toString()),
              book(),
            )
          }
        },
        BookOverviewCategory.FINISHED to buildMap {
          repeat(2) {
            put(
              BookId(Uuid.random().toString()),
              book(),
            )
          }
        },
      ),
      layoutMode = BookOverviewLayoutMode.List,
      playButtonState = BookOverviewViewState.PlayButtonState.Paused,
      showAddBookHint = false,
      showSearchIcon = true,
      isLoading = true,
      searchActive = true,
      searchViewState = BookSearchViewState.EmptySearch(
        books = listOf(book()),
        suggestedAuthors = emptyList(),
        recentQueries = emptyList(),
        query = "",
      ),
      showStoragePermissionBugCard = false,
      showFolderPickerIcon = true,
      dialog = null,
      selectedTab = Destination.Tab.LIBRARY,
    ),
  )
}
