package com.raywenderlich.emitron.data.bookmarks

import com.raywenderlich.emitron.model.Content
import com.raywenderlich.emitron.utils.async.ThreadManager
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Repository for bookmark operations
 */
class BookmarkRepository @Inject constructor(
  private val api: BookmarkApi,
  private val threadManager: ThreadManager
) {

  /**
   * Create a bookmark
   *
   * @param contentId Content id to be bookmarked
   *
   * @return Pair of response [Content] and True/False if request was succeeded/failed
   */
  suspend fun createBookmark(contentId: String): Pair<Content?, Boolean> {
    val bookmark = Content.newBookmark(contentId)
    return withContext(threadManager.io) {
      val response = api.createBookmark(bookmark)
      response.body() to response.isSuccessful
    }
  }

  /**
   * Delete a bookmark
   *
   * @param bookmarkId Bookmark id to be deleted
   *
   * @return True, if request was successful, else False
   */
  suspend fun deleteBookmark(bookmarkId: String): Boolean {
    return withContext(threadManager.io) {
      val response = api.deleteBookmark(bookmarkId)
      response.isSuccessful
    }
  }
}
