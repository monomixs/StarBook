package com.starbook.core.search

import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first
import com.starbook.core.data.Book
import com.starbook.core.data.repo.BookRepository
import com.starbook.core.data.repo.internals.dao.BookContentDao
import com.starbook.core.logging.api.Logger

@Inject
class BookSearch(
  private val dao: BookContentDao,
  private val repo: BookRepository,
) {
  suspend fun search(query: String): List<Book> {
    if (query.isBlank()) return emptyList()

    val matchQuery = buildString {
      append(
        query.trim()
          .replace("[^\\p{L}0-9\\s]".toRegex(), " ")
          .split("\\s+".toRegex())
          .filter { it.isNotEmpty() }
          .joinToString(" ") { "$it*" },
      )
    }

    val ftsResults = try {
      dao.search(matchQuery).mapNotNull { repo.get(it) }
    } catch (e: Exception) {
      Logger.e(e, "FTS search failed")
      emptyList()
    }

    if (ftsResults.isNotEmpty()) return ftsResults

    // Fallback: search in repo directly if FTS fails or returns no results
    val allBooks = repo.flow().first()
    return allBooks.filter { book ->
      book.content.name.contains(query, ignoreCase = true) ||
          book.content.author?.contains(query, ignoreCase = true) == true
    }
  }
}

