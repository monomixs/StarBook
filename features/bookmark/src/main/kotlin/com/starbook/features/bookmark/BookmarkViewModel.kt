package com.starbook.features.bookmark

import android.content.Context
import android.text.format.DateUtils
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.datastore.core.DataStore
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.starbook.core.common.DispatcherProvider
import com.starbook.core.common.MainScope
import com.starbook.core.data.BookId
import com.starbook.core.data.Bookmark
import com.starbook.core.data.KioskModeDemoData
import com.starbook.core.data.markForPosition
import com.starbook.core.data.repo.BookRepository
import com.starbook.core.data.repo.BookmarkRepo
import com.starbook.core.data.store.CurrentBookStore
import com.starbook.core.featureflag.FeatureFlag
import com.starbook.core.featureflag.KioskModeFeatureFlagQualifier
import com.starbook.core.playback.PlayerController
import com.starbook.core.playback.playstate.PlayStateManager
import com.starbook.core.strings.R
import com.starbook.core.ui.formatTime
import com.starbook.navigation.Navigator
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

@AssistedInject
class BookmarkViewModel(
  @CurrentBookStore
  private val currentBookStore: DataStore<BookId?>,
  private val repo: BookRepository,
  private val bookmarkRepo: BookmarkRepo,
  private val playStateManager: PlayStateManager,
  private val playerController: PlayerController,
  private val navigator: Navigator,
  private val context: Context,
  @KioskModeFeatureFlagQualifier
  private val kioskModeFeatureFlag: FeatureFlag<Boolean>,
  dispatcherProvider: DispatcherProvider,
  @Assisted
  private val bookId: BookId,
) {

  private val scope = MainScope(dispatcherProvider)

  var shouldScrollTo by mutableStateOf<Bookmark.Id?>(null)
    private set
  var dialogViewState: BookmarkDialogViewState by mutableStateOf(BookmarkDialogViewState.None)
    private set

  val state: StateFlow<BookmarkViewState> = if (kioskModeFeatureFlag.get()) {
    flowOf(kioskModeViewState()).stateIn(scope, SharingStarted.Eagerly, BookmarkViewState(emptyList(), null, BookmarkDialogViewState.None))
  } else {
    combine(
      repo.flow(bookId).flatMapLatest { book ->
        if (book == null) flowOf(emptyList<Bookmark>() to emptyList<com.starbook.core.data.Chapter>())
        else bookmarkRepo.bookmarksFlow(book.content).map { it.sortedByDescending { b -> b.addedAt } to book.chapters }
      },
      snapshotFlow { shouldScrollTo },
      snapshotFlow { dialogViewState }
    ) { (bookmarks, chapters), shouldScrollTo, dialog ->
      BookmarkViewState(
        bookmarks = bookmarks.map { bookmark ->
          val currentChapter = chapters.single { it.id == bookmark.chapterId }
          val bookmarkTitle = bookmark.title
          val title: String = when {
            bookmark.setBySleepTimer -> {
              val justNowThreshold = 1.minutes
              if (ChronoUnit.MILLIS.between(bookmark.addedAt, Instant.now()).milliseconds < justNowThreshold) {
                context.getString(R.string.bookmark_created_just_now)
              } else {
                DateUtils.getRelativeDateTimeString(
                  context,
                  bookmark.addedAt.toEpochMilli(),
                  justNowThreshold.inWholeMilliseconds,
                  2.days.inWholeMilliseconds,
                  0,
                ).toString()
              }
            }
            !bookmarkTitle.isNullOrEmpty() -> bookmarkTitle
            else -> currentChapter.markForPosition(bookmark.time).name ?: ""
          }

          BookmarkItemViewState(
            title = title,
            subtitle = formatTime(bookmark.time),
            id = bookmark.id,
            showSleepIcon = bookmark.setBySleepTimer,
          )
        },
        shouldScrollTo = shouldScrollTo,
        dialogViewState = dialog,
      )
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), BookmarkViewState(emptyList(), null, BookmarkDialogViewState.None))
  }

  private fun kioskModeViewState(): BookmarkViewState {
    return BookmarkViewState(
      bookmarks = KioskModeDemoData.bookmarkScreen.items.mapIndexed { index, item ->
        BookmarkItemViewState(
          title = item.title,
          subtitle = item.timestamp,
          id = Bookmark.Id(Uuid.parse("00000000-0000-0000-0000-${(index + 1).toString().padStart(12, '0')}")),
          showSleepIcon = false,
        )
      },
      shouldScrollTo = null,
      dialogViewState = BookmarkDialogViewState.None,
    )
  }

  fun deleteBookmark(id: Bookmark.Id) {
    scope.launch {
      bookmarkRepo.deleteBookmark(id)
    }
  }

  fun selectBookmark(id: Bookmark.Id) {
    scope.launch {
      // We need the full Bookmark object to get chapterId and start time.
      // Since we refactored, we should probably just query it or keep it in state.
      // For optimization, I'll add a helper to BookmarkRepo.
      val bookmark = bookmarkRepo.getBookmark(id) ?: return@launch

      val wasPlaying = playStateManager.playState == PlayStateManager.PlayState.Playing

      currentBookStore.updateData { bookId }
      playerController.setPosition(bookmark.time, bookmark.chapterId)

      if (wasPlaying) {
        playerController.play()
      }

      navigator.goBack()
    }
  }

  fun editBookmark(
    id: Bookmark.Id,
    newTitle: String,
  ) {
    scope.launch {
      bookmarkRepo.getBookmark(id)?.let {
        val withNewTitle = it.copy(
          title = newTitle,
          setBySleepTimer = false,
        )
        bookmarkRepo.addBookmark(withNewTitle)
      }
    }
  }

  fun addBookmark(name: String) {
    scope.launch {
      val book = repo.get(bookId) ?: return@launch
      val newBookmark = bookmarkRepo.addBookmarkAtBookPosition(
        book = book,
        title = name,
        setBySleepTimer = false,
      )
      shouldScrollTo = newBookmark.id
    }
  }

  fun onScrollConfirm() {
    shouldScrollTo = null
  }

  fun closeDialog() {
    dialogViewState = BookmarkDialogViewState.None
  }

  fun onAddClick() {
    dialogViewState = BookmarkDialogViewState.AddBookmark
  }

  fun onEditClick(id: Bookmark.Id) {
    scope.launch {
        val bookmark = bookmarkRepo.getBookmark(id) ?: return@launch
        dialogViewState = BookmarkDialogViewState.EditBookmark(id, bookmark.title)
    }
  }

  fun closeScreen() {
    navigator.goBack()
  }

  @AssistedFactory
  interface Factory {
    fun create(bookId: BookId): BookmarkViewModel
  }
}
