package com.starbook.core.playback.session.search

import android.provider.MediaStore
import androidx.datastore.core.DataStore
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first
import com.starbook.core.data.Book
import com.starbook.core.data.BookId
import com.starbook.core.data.repo.BookRepository
import com.starbook.core.data.store.CurrentBookStore
import com.starbook.core.logging.api.Logger

@Inject
class BookSearchHandler(
  private val repo: BookRepository,
  @CurrentBookStore
  private val currentBookStore: DataStore<BookId?>,
) {

  suspend fun handle(search: StarBookSearch): Book? {
    Logger.i("handle $search")
    return when (search.mediaFocus) {
      MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE -> searchByArtist(search)
      MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE,
      MediaStore.Audio.Media.ENTRY_CONTENT_TYPE,
      -> searchAlbum(search)
      else -> null
    } ?: searchUnstructured(search.query)
  }

  private suspend fun searchAlbum(search: StarBookSearch): Book? {
    if (search.album != null) {
      val foundMatch = findBook {
        val nameMatches = it.content.name.contains(search.album, ignoreCase = true)
        val artistMatches =
          search.artist == null || it.content.author?.contains(search.artist, ignoreCase = true) == true
        nameMatches && artistMatches
      }
      if (foundMatch != null) return foundMatch
    }

    return null
  }

  // Look for anything that might match the query
  private suspend fun searchUnstructured(query: String?): Book? {
    if (!query.isNullOrBlank()) {
      val foundMatch = findBook {
        val bookNameMatches = it.content.name.contains(query, ignoreCase = true)
        val authorMatches = it.content.author?.contains(query, ignoreCase = true) == true
        val chapterNameMatches = it.chapters.any { chapter ->
          val chapterName = chapter.name
          chapterName != null && chapterName.contains(query, ignoreCase = true)
        }
        bookNameMatches || authorMatches || chapterNameMatches
      }
      if (foundMatch != null) return foundMatch
    }

    Logger.i("continuing from search without query")
    val currentId = currentBookStore.data.first()
    return findBook { it.content.id == currentId }
  }

  private suspend fun searchByArtist(search: StarBookSearch): Book? {
    Logger.i("searchByArtist")
    if (search.artist != null) {
      val foundMatch = findBook {
        it.content.author?.contains(search.artist, ignoreCase = true) == true
      }
      if (foundMatch != null) {
        return foundMatch
      }
    }
    return null
  }

  // Play the first book that matches to a selector. Returns if a book is being played
  private suspend inline fun findBook(selector: (Book) -> Boolean): Book? {
    return repo.all().find(selector)
  }
}

