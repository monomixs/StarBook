package com.starbook.features.bookOverview.metadata

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.starbook.core.data.BookContent
import com.starbook.core.data.BookId
import com.starbook.core.data.toUri
import com.starbook.core.data.repo.BookContentRepo
import com.starbook.core.data.repo.BookRepository
import com.starbook.core.data.repo.CoverCacheManager
import com.starbook.core.scanner.CoverSaver
import com.starbook.core.scanner.TagWriter
import com.starbook.features.bookOverview.di.BookOverviewScope
import com.starbook.navigation.Navigator
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@SingleIn(BookOverviewScope::class)
@Inject
class MetadataEditorViewModel(
  private val repo: BookRepository,
  private val contentRepo: BookContentRepo,
  private val coverSaver: CoverSaver,
  private val coverCacheManager: CoverCacheManager,
  private val tagWriter: TagWriter,
  private val navigator: Navigator,
) : ViewModel() {

  var bookId by mutableStateOf<BookId?>(null)
    private set

  var title by mutableStateOf("")
  var author by mutableStateOf("")
  var genre by mutableStateOf("")
  var coverUrl by mutableStateOf<String?>(null)

  private var originalContent: BookContent? = null
  private var newCoverFile: File? = null

  fun load(id: BookId) {
    if (this.bookId == id) return
    this.bookId = id
    viewModelScope.launch {
      val book = repo.get(id)
      originalContent = book?.content
      title = book?.content?.name ?: ""
      author = book?.content?.author ?: ""
      genre = book?.content?.genre ?: ""
      coverUrl = book?.content?.coverUrl
    }
  }

  fun onSave() {
    val id = bookId ?: return
    viewModelScope.launch {
      try {
        withContext(Dispatchers.IO) {
          val book = repo.get(id) ?: return@withContext
          val current = originalContent ?: book.content

          // Write metadata to files
          book.chapters.forEach { chapter ->
            tagWriter.writeMetadata(
              uri = chapter.id.toUri(),
              title = title.trim(),
              author = author.trim().takeIf { it.isNotBlank() },
              genre = genre.trim().takeIf { it.isNotBlank() }
            )
          }

          // Handle cover update
          newCoverFile?.let { file ->
            current.cover?.let { oldFile ->
              coverCacheManager.cacheOldCover(id, oldFile)
            }
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (bitmap != null) {
              coverSaver.save(id, bitmap)
            }
          }

          val updated = repo.get(id)?.content?.copy(
            name = title.trim(),
            author = author.trim().takeIf { it.isNotBlank() },
            genre = genre.trim().takeIf { it.isNotBlank() }
          )
          if (updated != null) {
            contentRepo.put(updated)
          }
        }
      } catch (e: Exception) {
        com.starbook.core.logging.api.Logger.w(e, "Failed to save metadata")
      } finally {
        navigator.goBack()
      }
    }
  }

  fun onRestore() {
    val id = bookId
    val cachedCover = if (id != null) coverCacheManager.getLatestCachedCover(id) else null
    if (cachedCover != null) {
      coverUrl = cachedCover.toURI().toString()
      newCoverFile = cachedCover
    } else {
      originalContent?.let {
        title = it.name
        author = it.author ?: ""
        genre = it.genre ?: ""
        coverUrl = it.coverUrl
        newCoverFile = null
      }
    }
  }

  fun onImagePicked(file: File) {
    newCoverFile = file
    coverUrl = file.toURI().toString()
  }

  fun onBack() {
    navigator.goBack()
  }
}
