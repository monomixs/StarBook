package com.starbook.features.bookOverview.editTitle

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import com.starbook.core.data.BookId
import com.starbook.core.data.repo.BookRepository
import com.starbook.features.bookOverview.di.BookOverviewScope

@SingleIn(BookOverviewScope::class)
@Inject
class EditBookTitleViewModel(private val repo: BookRepository) {

  private val scope = MainScope()

  private val _state = mutableStateOf<EditBookTitleState?>(null)
  internal val state: State<EditBookTitleState?> get() = _state

  internal fun onEditTitle(bookId: BookId) {
    scope.launch {
      val book = repo.get(bookId) ?: return@launch
      _state.value = EditBookTitleState(
        title = book.content.name,
        bookId = bookId,
      )
    }
  }

  internal fun onDismissEditTitle() {
    _state.value = null
  }

  internal fun onUpdateEditTitle(title: String) {
    _state.value = _state.value?.copy(title = title)
  }

  internal fun onConfirmEditTitle() {
    val state = _state.value
    if (state != null) {
      check(state.confirmButtonEnabled)
      scope.launch {
        repo.updateBook(state.bookId) {
          it.copy(name = state.title.trim())
        }
      }
    }
    _state.value = null
  }
}

