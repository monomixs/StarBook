package com.starbook.core.data.repo

import com.starbook.core.data.Book
import com.starbook.core.data.BookContent
import com.starbook.core.data.Bookmark
import kotlinx.coroutines.flow.Flow

public interface BookmarkRepo {
  public suspend fun deleteBookmark(id: Bookmark.Id)

  public suspend fun addBookmark(bookmark: Bookmark)

  public suspend fun getBookmark(id: Bookmark.Id): Bookmark?

  @IgnorableReturnValue
  public suspend fun addBookmarkAtBookPosition(
    book: Book,
    title: String?,
    setBySleepTimer: Boolean,
  ): Bookmark

  public suspend fun bookmarks(book: BookContent): List<Bookmark>

  public fun bookmarksFlow(book: BookContent): Flow<List<Bookmark>>
}

