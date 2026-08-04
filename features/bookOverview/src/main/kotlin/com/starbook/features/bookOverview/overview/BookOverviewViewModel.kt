package com.starbook.features.bookOverview.overview

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import com.starbook.core.common.AppInfoProvider
import com.starbook.core.common.DispatcherProvider
import com.starbook.core.common.MainScope
import com.starbook.core.common.comparator.sortedNaturally
import com.starbook.core.data.Book
import com.starbook.core.data.BookId
import com.starbook.core.data.GridMode
import com.starbook.core.data.KioskModeDemoData
import com.starbook.core.data.repo.BookContentRepo
import com.starbook.core.data.repo.BookRepository
import com.starbook.core.data.repo.internals.dao.RecentBookSearchDao
import com.starbook.core.data.store.CurrentBookStore
import com.starbook.core.data.store.FolderPickerMovedDialogShownStore
import com.starbook.core.data.store.GridModeStore
import com.starbook.core.featureflag.ExperimentalPlaybackPersistenceQualifier
import com.starbook.core.featureflag.FeatureFlag
import com.starbook.core.featureflag.FolderPickerInSettingsFeatureFlagQualifier
import com.starbook.core.featureflag.KioskModeFeatureFlagQualifier
import com.starbook.core.playback.PlayerController
import com.starbook.core.playback.playstate.PlayStateManager
import com.starbook.core.scanner.DeviceHasStoragePermissionBug
import com.starbook.core.scanner.MediaScanTrigger
import com.starbook.core.search.BookSearch
import com.starbook.core.ui.GridCount
import com.starbook.features.bookOverview.di.BookOverviewScope
import com.starbook.features.bookOverview.search.BookSearchViewState
import com.starbook.navigation.Destination
import com.starbook.navigation.Navigator
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Instant

@SingleIn(BookOverviewScope::class)
@Inject
class BookOverviewViewModel(
  private val repo: BookRepository,
  private val mediaScanner: MediaScanTrigger,
  private val playStateManager: PlayStateManager,
  private val playerController: PlayerController,
  @CurrentBookStore
  private val currentBookStoreDataStore: DataStore<BookId?>,
  @FolderPickerMovedDialogShownStore
  private val folderPickerMovedDialogShownStore: DataStore<Boolean>,
  @GridModeStore
  private val gridModeStore: DataStore<GridMode>,
  private val gridCount: GridCount,
  private val navigator: Navigator,
  private val appInfoProvider: AppInfoProvider,
  private val recentBookSearchDao: RecentBookSearchDao,
  private val search: BookSearch,
  private val contentRepo: BookContentRepo,
  private val deviceHasStoragePermissionBug: DeviceHasStoragePermissionBug,
  @FolderPickerInSettingsFeatureFlagQualifier
  private val folderPickerInSettingsFeatureFlag: FeatureFlag<Boolean>,
  @ExperimentalPlaybackPersistenceQualifier
  private val experimentalPlaybackPersistenceFeatureFlag: FeatureFlag<Boolean>,
  @KioskModeFeatureFlagQualifier
  private val kioskModeFeatureFlag: FeatureFlag<Boolean>,
  dispatcherProvider: DispatcherProvider,
) {

  private val scope = MainScope(dispatcherProvider)
  var searchActive by mutableStateOf(false)
    private set
  var query by mutableStateOf("")
    private set
  var selectedTab by mutableStateOf(Destination.Tab.LIBRARY)
    private set
  var dialog by mutableStateOf<BookOverviewViewState.Dialog?>(null)
    private set

  fun attach() {
    mediaScanner.scan()
  }

  private data class MainParams(
    val playState: PlayStateManager.PlayState,
    val hasStoragePermissionBug: Boolean,
    val allBooks: List<Book>,
    val currentBookId: BookId?,
    val scannerActive: Boolean,
    val folderPickerMovedDialogShown: Boolean,
    val gridMode: GridMode?
  )

  private data class UiParams(
    val searchActive: Boolean,
    val query: String,
    val selectedTab: Destination.Tab,
    val dialog: BookOverviewViewState.Dialog?
  )

  val state: StateFlow<BookOverviewViewState> = if (kioskModeFeatureFlag.get()) {
    flowOf(kioskModeState()).stateIn(scope, SharingStarted.Eagerly, BookOverviewViewState.Loading)
  } else {
    combine(
      combine(
        playStateManager.playStateFlow,
        deviceHasStoragePermissionBug.hasBug,
        repo.flow(),
        currentBookStoreDataStore.data,
        mediaScanner.scannerActive,
        folderPickerMovedDialogShownStore.data,
        gridModeStore.data
      ) { params: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        MainParams(
          playState = params[0] as PlayStateManager.PlayState,
          hasStoragePermissionBug = params[1] as Boolean,
          allBooks = params[2] as List<Book>,
          currentBookId = params[3] as BookId?,
          scannerActive = params[4] as Boolean,
          folderPickerMovedDialogShown = params[5] as Boolean,
          gridMode = params[6] as GridMode?
        )
      },
      combine(
        snapshotFlow { searchActive },
        snapshotFlow { query },
        snapshotFlow { selectedTab },
        snapshotFlow { dialog }
      ) { searchActive, query, tab, dialog ->
        UiParams(searchActive, query, tab, dialog)
      },
      snapshotFlow { query }.flatMapLatest { q ->
        if (q.isBlank()) flowOf(emptyList<Book>())
        else flow { emit(search.search(q)) }
      },
      recentBookSearchDao.recentBookSearches().map { it.reversed() }
    ) { main, ui, searchResults, recentSearches ->
      if (main.gridMode == null) return@combine BookOverviewViewState.Loading

      val layoutMode = when (main.gridMode) {
        GridMode.LIST -> BookOverviewLayoutMode.List
        GridMode.GRID -> BookOverviewLayoutMode.Grid
        GridMode.FOLLOW_DEVICE -> if (gridCount.useGridAsDefault()) BookOverviewLayoutMode.Grid else BookOverviewLayoutMode.List
      }

      val booksByState = main.allBooks
        .groupBy { it.category }
        .mapValues { (category, books) ->
          books
            .sortedWith(category.comparator)
            .associate { book ->
              book.id to book.toItemViewState()
            }
        }
        .toSortedMap()

      val searchViewState = if (ui.searchActive) {
        if (ui.query.isNotBlank()) {
          BookSearchViewState.SearchResults(
            query = ui.query,
            books = searchResults.map { it.toItemViewState() },
            layoutMode = layoutMode,
          )
        } else {
          val suggestedAuthors = main.allBooks
            .mapNotNull { it.content.author }
            .toSet()
            .sortedNaturally()
          BookSearchViewState.EmptySearch(
            books = main.allBooks.map { it.toItemViewState() },
            recentQueries = recentSearches,
            suggestedAuthors = suggestedAuthors,
            query = ui.query,
          )
        }
      } else {
        BookSearchViewState.EmptySearch(emptyList(), emptyList(), emptyList(), ui.query)
      }

      BookOverviewViewState(
        layoutMode = layoutMode,
        books = booksByState,
        playButtonState = if (main.playState == PlayStateManager.PlayState.Playing) {
          BookOverviewViewState.PlayButtonState.Playing
        } else {
          BookOverviewViewState.PlayButtonState.Paused
        }.takeIf { main.currentBookId != null },
        showAddBookHint = if (main.hasStoragePermissionBug) false else !main.scannerActive && main.allBooks.isEmpty(),
        showSearchIcon = main.allBooks.isNotEmpty(),
        isLoading = main.scannerActive,
        searchActive = ui.searchActive,
        searchViewState = searchViewState,
        showStoragePermissionBugCard = main.hasStoragePermissionBug,
        showFolderPickerIcon = !folderPickerInSettingsFeatureFlag.get() &&
          !main.folderPickerMovedDialogShown &&
          appInfoProvider.installTime < FolderPickerMigrationInstallTimeCutoff,
        dialog = ui.dialog,
        selectedTab = ui.selectedTab,
      )
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), BookOverviewViewState.Loading)
  }

  fun onTabClick(tab: Destination.Tab) {
    selectedTab = tab
  }

  fun onGoToLibrary() {
    navigator.setRoot(Destination.BookOverview)
  }

  private fun kioskModeState(): BookOverviewViewState {
    return BookOverviewViewState(
      layoutMode = BookOverviewLayoutMode.List,
      books = mapOf(
        BookOverviewCategory.CURRENT to KioskModeDemoData.demoAudiobooks.associate { book ->
          book.id to BookOverviewItemViewState(
            name = book.title,
            author = book.author,
            cover = book.coverUrl,
            progress = book.progress / 100F,
            id = book.id,
            remainingTime = book.remaining,
            genre = null,
            duration = null,
            category = BookOverviewCategory.CURRENT,
          )
        },
      ),
      playButtonState = BookOverviewViewState.PlayButtonState.Paused,
      showAddBookHint = false,
      showSearchIcon = true,
      isLoading = false,
      searchActive = false,
      searchViewState = BookSearchViewState.EmptySearch(
        books = KioskModeDemoData.demoAudiobooks.map { book ->
          BookOverviewItemViewState(
            name = book.title,
            author = book.author,
            cover = book.coverUrl,
            progress = book.progress / 100F,
            id = book.id,
            remainingTime = book.remaining,
            genre = null,
            duration = null,
            category = BookOverviewCategory.CURRENT,
          )
        },
        recentQueries = emptyList(),
        suggestedAuthors = KioskModeDemoData.demoAudiobooks.map { it.author },
        query = "",
      ),
      showStoragePermissionBugCard = false,
      showFolderPickerIcon = false,
      dialog = null,
      selectedTab = Destination.Tab.LIBRARY,
    )
  }

  fun onSettingsClick() {
    navigator.goTo(Destination.Settings)
  }

  fun onBookClick(id: BookId) {
    scope.launch {
      currentBookStoreDataStore.updateData { id }
      playerController.play()
    }
  }

  fun onBookFolderClick() {
    dialog = BookOverviewViewState.Dialog.FolderPickerMovedToSettings
  }

  fun onFolderPickerMovedDialogDismiss() {
    dialog = null
    scope.launch {
      folderPickerMovedDialogShownStore.updateData { true }
    }
  }

  fun onSearchActiveChange(active: Boolean) {
    if (active && !searchActive) {
      query = ""
    }
    this.searchActive = active
  }

  fun onSearchQueryChange(query: String) {
    this.query = query
  }

  fun onSearchBookClick(id: BookId) {
    val query = query.trim()
    if (query.isNotBlank()) {
      scope.launch {
        recentBookSearchDao.add(query)
      }
    }
    searchActive = false
    navigator.goTo(Destination.Playback(id))
  }

  fun playPause() {
    playerController.playPause()
  }

  fun onPermissionBugCardClick() {
    if (Build.VERSION.SDK_INT >= 30) {
      navigator.goTo(
        Destination.Activity(
          Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            .setData("package:com.android.externalstorage".toUri()),
        ),
      )
    }
  }
}

private val FolderPickerMigrationInstallTimeCutoff = Instant.parse("2026-06-17T00:00:00Z")
