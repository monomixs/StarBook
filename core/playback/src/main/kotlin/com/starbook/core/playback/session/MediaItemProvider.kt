package com.starbook.core.playback.session

import android.app.Application
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.ClippingConfiguration
import androidx.media3.session.MediaSession.MediaItemsWithStartPosition
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import com.starbook.core.data.Book
import com.starbook.core.data.BookComparator
import com.starbook.core.data.BookContent
import com.starbook.core.data.BookId
import com.starbook.core.data.Chapter
import com.starbook.core.data.durationMs
import com.starbook.core.data.repo.BookContentRepo
import com.starbook.core.data.repo.BookRepository
import com.starbook.core.data.repo.ChapterRepo
import com.starbook.core.data.store.CurrentBookStore
import com.starbook.core.data.toUri
import java.io.File
import com.starbook.core.strings.R as StringsR

@Inject
class MediaItemProvider(
  private val bookRepository: BookRepository,
  private val application: Application,
  private val chapterRepo: ChapterRepo,
  private val contentRepo: BookContentRepo,
  private val imageFileProvider: ImageFileProvider,
  @CurrentBookStore
  private val currentBookStoreId: DataStore<BookId?>,
) {

  fun root(): MediaItem = buildMediaItem(
    title = application.getString(StringsR.string.media_session_library_root),
    browsable = true,
    isPlayable = false,
    mediaId = MediaId.Root,
    mediaType = StarBookMediaType.AudioBookRoot,
  )

  fun recent(): MediaItem? = buildMediaItem(
    title = application.getString(StringsR.string.media_session_library_recent),
    browsable = true,
    isPlayable = false,
    mediaId = MediaId.Recent,
    mediaType = StarBookMediaType.AudioBook,
  ).takeIf { runBlocking { currentBookStoreId.data.first() != null } }

  suspend fun item(id: String): MediaItem? {
    val mediaId = id.toMediaIdOrNull() ?: return null
    return when (mediaId) {
      MediaId.Root -> root()
      is MediaId.Book -> {
        bookRepository.get(mediaId.id)?.let { toMediaItem(it) }
      }
      is MediaId.Chapter -> {
        val content = contentRepo.get(mediaId.bookId) ?: return null
        chapterRepo.get(mediaId.chapterId)?.let {
          toMediaItem(it, content)
        }
      }
      is MediaId.ChapterMark -> {
        val content = contentRepo.get(mediaId.bookId) ?: return null
        val chapter = chapterRepo.get(mediaId.chapterId) ?: return null
        val mark = chapter.chapterMarks.getOrNull(mediaId.markIndex) ?: return null
        toMediaItem(
          playbackItem = PlaybackItem(
            index = 0,
            bookId = mediaId.bookId,
            chapter = chapter,
            markIndex = mediaId.markIndex,
            mark = mark,
          ),
          content = content,
        )
      }
      MediaId.Recent -> recent()
    }
  }

  fun mediaItemsWithStartPosition(book: Book): MediaItemsWithStartPosition {
    return MediaItemsWithStartPosition(
      listOf(toMediaItem(book)),
      C.INDEX_UNSET,
      C.TIME_UNSET,
    )
  }

  suspend fun mediaItemsWithStartPosition(id: String): MediaItemsWithStartPosition? {
    return when (val mediaId = id.toMediaIdOrNull()) {
      is MediaId.Book -> {
        val book = bookRepository.get(mediaId.id) ?: return null
        mediaItemsWithStartPosition(book)
      }
      is MediaId.Chapter, is MediaId.ChapterMark, MediaId.Root, MediaId.Recent, null -> null
    }
  }

  suspend fun chapters(bookId: BookId): List<MediaItem>? {
    val book = bookRepository.get(bookId) ?: return null
    return playbackItems(book)
  }

  internal fun playbackItems(book: Book): List<MediaItem> {
    return book.playbackItems().map { playbackItem ->
      toMediaItem(playbackItem, book.content)
    }
  }

  suspend fun children(id: String): List<MediaItem>? {
    val mediaId = id.toMediaIdOrNull() ?: return null
    return when (mediaId) {
      MediaId.Root -> {
        bookRepository.all()
          .sortedWith(BookComparator.ByLastPlayed)
          .map { book ->
            toMediaItem(book)
          }
      }
      is MediaId.Book -> chapters(mediaId.id)
      is MediaId.Chapter, is MediaId.ChapterMark -> null
      MediaId.Recent -> {
        val bookId = currentBookStoreId.data.first() ?: return null
        val book = bookRepository.get(bookId) ?: return null
        listOf(toMediaItem(book))
      }
    }
  }

  fun toMediaItem(book: Book): MediaItem = buildMediaItem(
    title = book.content.name,
    mediaId = MediaId.Book(book.id),
    browsable = false,
    isPlayable = true,
    imageUri = book.content.cover?.toProvidedUri(),
    mediaType = StarBookMediaType.AudioBook,
  )

  private fun toMediaItem(
    chapter: Chapter,
    content: BookContent,
  ) = buildMediaItem(
    title = chapter.name ?: chapter.id.value,
    mediaId = MediaId.Chapter(bookId = content.id, chapterId = chapter.id),
    browsable = false,
    isPlayable = true,
    sourceUri = chapter.id.toUri(),
    imageUri = content.cover?.toProvidedUri(),
    artist = content.author,
    mediaType = StarBookMediaType.AudioBookChapter,
  )

  private fun toMediaItem(
    playbackItem: PlaybackItem,
    content: BookContent,
  ) = buildMediaItem(
    title = playbackItem.mark.name
      ?: playbackItem.chapter.name
      ?: playbackItem.chapter.id.value,
    mediaId = playbackItem.mediaId,
    browsable = false,
    isPlayable = true,
    sourceUri = playbackItem.chapter.id.toUri(),
    imageUri = content.cover?.toProvidedUri(),
    artist = content.author,
    durationMs = playbackItem.mark.durationMs,
    clippingConfiguration = ClippingConfiguration.Builder()
      .setStartPositionMs(playbackItem.mark.startMs)
      .setEndPositionMs(playbackItem.mark.endMs)
      .build(),
    mediaType = StarBookMediaType.AudioBookChapter,
  )

  private fun File.toProvidedUri(): Uri = imageFileProvider.uri(this)
}
