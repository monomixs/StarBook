package com.starbook.core.data.repo

import android.content.Context
import com.starbook.core.data.BookId
import com.starbook.core.logging.api.Logger
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.time.Duration.Companion.minutes

@SingleIn(AppScope::class)
@Inject
public class CoverCacheManager(context: Context) {
  private val cacheDir = File(context.cacheDir, "cover_history")
  private val scope = CoroutineScope(Dispatchers.IO)

  init {
    if (!cacheDir.exists()) {
      cacheDir.mkdirs()
    }
  }

  public fun cacheOldCover(bookId: BookId, file: File) {
    scope.launch {
      try {
        val cachedFile = File(cacheDir, "cache_${bookId.value.hashCode()}_${System.currentTimeMillis()}.png")
        file.copyTo(cachedFile, overwrite = true)
        Logger.d("Cached old cover to ${cachedFile.absolutePath}")
        delay(30.minutes)
        if (cachedFile.exists()) {
          cachedFile.delete()
          Logger.d("Deleted cached old cover ${cachedFile.absolutePath}")
        }
      } catch (e: Exception) {
        Logger.e(e, "Error caching old cover")
      }
    }
  }

  public fun getLatestCachedCover(bookId: BookId): File? {
    val prefix = "cache_${bookId.value.hashCode()}_"
    return cacheDir.listFiles()
      ?.filter { it.name.startsWith(prefix) }
      ?.maxByOrNull { it.name.substringAfterLast("_").substringBefore(".").toLongOrNull() ?: 0L }
  }
}
