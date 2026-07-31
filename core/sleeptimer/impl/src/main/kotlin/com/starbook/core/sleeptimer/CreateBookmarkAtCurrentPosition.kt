package com.starbook.core.sleeptimer

import androidx.datastore.core.DataStore
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first
import com.starbook.core.data.BookId
import com.starbook.core.data.repo.BookmarkRepo
import com.starbook.core.data.store.CurrentBookStore
import com.starbook.core.playback.CurrentBookResolver

@Inject
class CreateBookmarkAtCurrentPosition(
  private val bookmarkRepo: BookmarkRepo,
  private val currentBookResolver: CurrentBookResolver,
  @CurrentBookStore
  private val currentBookStore: DataStore<BookId?>,
) {

  suspend fun create() {
    val currentBookId = currentBookStore.data.first() ?: return
    val currentBook = currentBookResolver.book(currentBookId) ?: return
    bookmarkRepo.addBookmarkAtBookPosition(
      book = currentBook,
      title = null,
      setBySleepTimer = true,
    )
  }
}

