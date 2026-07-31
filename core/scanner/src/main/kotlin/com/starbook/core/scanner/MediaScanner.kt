package com.starbook.core.scanner

import dev.zacsweers.metro.Inject
import com.starbook.core.data.BookId
import com.starbook.core.data.audioFileCount
import com.starbook.core.data.folders.FolderType
import com.starbook.core.data.isAudioFile
import com.starbook.core.data.repo.BookContentRepo
import com.starbook.core.documentfile.CachedDocumentFile
import com.starbook.core.documentfile.walk
import com.starbook.core.logging.api.Logger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@Inject
internal class MediaScanner(
  private val contentRepo: BookContentRepo,
  private val chapterParser: ChapterParser,
  private val bookParser: BookParser,
  private val deviceHasPermissionBug: DeviceHasStoragePermissionBug,
) {

  suspend fun scan(folders: Map<FolderType, List<CachedDocumentFile>>) = coroutineScope {
    val files = folders.flatMap { (folderType, files) ->
      when (folderType) {
        FolderType.SingleFile, FolderType.SingleFolder -> {
          files
        }
        FolderType.Root -> {
          files.flatMap { file ->
            file.children
          }
        }
        FolderType.Author -> {
          files.flatMap { folder ->
            folder.children.flatMap { author ->
              if (author.isFile) {
                listOf(author)
              } else {
                author.children.flatMap {
                  author.children
                }
              }
            }
          }
        }
      }
    }

    contentRepo.setAllInactiveExcept(files.map { BookId(it.uri) })

    val probeFile = folders.values.flatten().findProbeFile()
    if (probeFile != null) {
      if (deviceHasPermissionBug.checkForBugAndSet(probeFile)) {
        Logger.w("Device has permission bug, aborting scan! Probed $probeFile")
        return@coroutineScope
      }
    }

    // Process books in parallel for faster scanning
    files
      .sortedBy { it.audioFileCount() }
      .chunked(3)
      .forEach { chunk ->
        chunk.map { file ->
          async { scan(file) }
        }.awaitAll()
      }
  }

  private fun List<CachedDocumentFile>.findProbeFile(): CachedDocumentFile? {
    return asSequence().flatMap { it.walk() }
      .firstOrNull { child ->
        child.isAudioFile() && child.uri.authority == "com.android.externalstorage.documents"
      }
  }

  private suspend fun scan(file: CachedDocumentFile) {
    try {
      val parseResult = chapterParser.parse(file)
      val chapters = parseResult.chapters
      if (chapters.isEmpty()) return

      val content = bookParser.parseAndStore(chapters, file, parseResult.firstChapterMetadata)

      val chapterIds = chapters.map { it.id }
      val currentChapterGone = content.currentChapter !in chapterIds
      val currentChapter = if (currentChapterGone) chapterIds.first() else content.currentChapter
      val positionInChapter = if (currentChapterGone) 0 else content.positionInChapter
      val updated = content.copy(
        chapters = chapterIds,
        currentChapter = currentChapter,
        positionInChapter = positionInChapter,
        isActive = true,
      )
      if (content != updated) {
        validateIntegrity(updated, chapters)
        contentRepo.put(updated)
      }
    } catch (e: Exception) {
      Logger.e(e, "Failed to scan book: ${file.uri}")
    }
  }
}
