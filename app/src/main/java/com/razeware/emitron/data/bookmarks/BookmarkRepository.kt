package com.razeware.emitron.data.bookmarks

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.razeware.emitron.data.content.ContentDataSourceLocal
import com.razeware.emitron.model.Content
import com.razeware.emitron.model.Data
import com.razeware.emitron.utils.BoundaryCallbackNotifier
import com.razeware.emitron.utils.LocalPagedResponse
import com.razeware.emitron.utils.PagedBoundaryCallbackImpl
import com.razeware.emitron.utils.PagedResponse
import com.razeware.emitron.utils.async.ThreadManager
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Repository for bookmark operations
 */
class BookmarkRepository @Inject constructor(
  private val api: BookmarkApi,
  private val threadManager: ThreadManager,
  private val contentDataSourceLocal: ContentDataSourceLocal
) {

  companion object {
    const val PAGE_SIZE: Int = 10
  }

  /**
   * Create a bookmark
   *
   * @param contentId Content id to be bookmarked
   *
   * @return Pair of response [Content] and True/False if request was succeeded/failed
   */
  @WorkerThread
  @Throws(Exception::class)
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
  @WorkerThread
  @Throws(Exception::class)
  suspend fun deleteBookmark(contentId: String, bookmarkId: String): Boolean {
    return withContext(threadManager.io) {
      contentDataSourceLocal.updateBookmark(contentId, null)
      val response = api.deleteBookmark(bookmarkId)
      response.isSuccessful
    }
  }

  /**
   * Update bookmark id for content id
   *
   * @param contentId Content id
   * @param bookmarkId Bookmark id
   */
  @WorkerThread
  @Throws(Exception::class)
  suspend fun updateBookmarkInDb(contentId: String, bookmarkId: String?) {
    return withContext(threadManager.io) {
      contentDataSourceLocal.updateBookmark(contentId, bookmarkId)
    }
  }

  /**
   * Fetch bookmarks
   *
   * @return [PagedResponse] containing LiveData objects of network state,
   * initial meta data, retry callback and paged list
   */
  @MainThread
  fun getBookmarks(
    boundaryCallbackNotifier: BoundaryCallbackNotifier? = null
  ): LocalPagedResponse<Data> {

    val sourceFactory =
      contentDataSourceLocal.getBookmarks().map { it.toData() }

    val pagedListConfig = PagedList.Config.Builder()
      .setInitialLoadSizeHint(PAGE_SIZE)
      .setPageSize(PAGE_SIZE)
      .build()

    val boundaryCallback =
      BookmarkBoundaryCallback(
        api,
        contentDataSourceLocal,
        threadManager,
        boundaryCallbackNotifier,
        PagedBoundaryCallbackImpl()
      )

    val livePagedList =
      sourceFactory.toLiveData(
        config = pagedListConfig,
        fetchExecutor = threadManager.networkExecutor,
        boundaryCallback = boundaryCallback
      )

    return LocalPagedResponse(
      pagedList = livePagedList,
      uiState = boundaryCallback.uiState()
    )
  }
}
