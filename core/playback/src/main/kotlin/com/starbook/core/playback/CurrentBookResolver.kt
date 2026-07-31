package com.starbook.core.playback

import androidx.datastore.core.DataStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.first
import com.starbook.core.data.Book
import com.starbook.core.data.BookId
import com.starbook.core.data.repo.BookRepository
import com.starbook.core.data.store.CurrentBookStore
import com.starbook.core.featureflag.ExperimentalPlaybackPersistenceQualifier
import com.starbook.core.featureflag.FeatureFlag

@SingleIn(AppScope::class)
@Inject
class CurrentBookResolver(
  private val bookRepository: BookRepository,
  private val playerController: PlayerController,
  @CurrentBookStore
  private val currentBookStore: DataStore<BookId?>,
  @ExperimentalPlaybackPersistenceQualifier
  private val experimentalPlaybackPersistenceFeatureFlag: FeatureFlag<Boolean>,
) {

  suspend fun currentBook(): Book? {
    val bookId = currentBookStore.data.first() ?: return null
    return book(bookId)
  }

  suspend fun book(bookId: BookId): Book? {
    val book = bookRepository.get(bookId) ?: return null
    if (!experimentalPlaybackPersistenceFeatureFlag.get()) {
      return book
    }
    val livePosition = playerController.livePlaybackState(bookId) ?: return book
    return book.update {
      it.copy(
        currentChapter = livePosition.chapterId,
        positionInChapter = livePosition.positionMs,
      )
    }
  }
}

