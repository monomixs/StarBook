package com.starbook.features.bookOverview.bottomSheet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import com.starbook.core.data.Book
import com.starbook.core.data.BookId
import com.starbook.core.data.repo.BookRepository
import com.starbook.core.data.store.CurrentBookStore
import com.starbook.core.playback.PlayerController
import com.starbook.features.bookOverview.di.BookOverviewScope
import com.starbook.navigation.Destination
import com.starbook.navigation.Navigator
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@SingleIn(BookOverviewScope::class)
@Inject
class BottomSheetViewModel(
  private val repo: BookRepository,
  private val navigator: Navigator,
  private val playerController: PlayerController,
  @CurrentBookStore
  private val currentBookStoreDataStore: DataStore<BookId?>,
  private val itemViewModels: Set<@JvmSuppressWildcards BottomSheetItemViewModel>,
) {

  private val scope = MainScope()

  var bookId: BookId? = null
    private set

  var book by mutableStateOf<Book?>(null)
    private set

  internal var sheetState by mutableStateOf<EditBookBottomSheetState?>(null)
    private set

  internal fun bookSelected(bookId: BookId) {
    this.bookId = bookId
    scope.launch {
      book = repo.get(bookId)
      val items = itemViewModels.flatMap { it.items(bookId) }
      sheetState = EditBookBottomSheetState(items)
    }
  }

  internal fun onResumeClick() {
    val id = bookId ?: return
    scope.launch {
      currentBookStoreDataStore.updateData { id }
      playerController.play()
    }
  }

  internal fun onEditClick() {
    val id = bookId ?: return
    navigator.goTo(Destination.MetadataEditor(id))
  }

  internal fun onItemClick(item: BottomSheetItem) {
    val id = bookId ?: return
    scope.launch {
      itemViewModels.forEach { it.onItemClick(id, item) }
    }
  }
}
