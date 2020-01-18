package com.razeware.emitron.data.bookmarks

import androidx.paging.PagedList
import com.razeware.emitron.data.content.ContentDataSourceLocal
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.DataType
import com.razeware.emitron.utils.*
import com.razeware.emitron.utils.async.ThreadManager
import java.io.IOException
import javax.inject.Inject

/**
 * Bookmark boundary callback to fetch bookmarks when no data is in database,
 * or last item of database has been queried.
 */
class BookmarkBoundaryCallback @Inject constructor(
  private val bookmarkApi: BookmarkApi,
  private val contentLocalDataSource: ContentDataSourceLocal,
  private val threadManager: ThreadManager,
  private val boundaryCallbackNotifier: BoundaryCallbackNotifier?,
  pagedBoundaryCallback: PagedBoundaryCallbackImpl
) : PagedList.BoundaryCallback<Data>(), PagedBoundaryCallback by pagedBoundaryCallback {

  companion object {
    private const val NETWORK_PAGE_SIZE: Int = 10
  }

  /**
   * See [PagedList.BoundaryCallback.onZeroItemsLoaded]
   */
  override fun onZeroItemsLoaded() {
    /**
     * If a content update/delete request is in progress, we will skip loading from network
     */
    if (boundaryCallbackNotifier.hasRequests()) {
      updateUiState(UiStateManager.UiState.INIT_EMPTY)
      return
    }
    /**
     * Once content is update/deleted the page number should be pageReset,
     * as server will have the latest data
     */
    if (boundaryCallbackNotifier.shouldReset()) {
      updatePageNumber(0)
    }
    updateUiState(UiStateManager.UiState.INIT)
    updateCallbackType(PagedBoundaryCallback.BoundaryCallbackType.INIT)
    requestAndSaveBookmarks()
  }

  /**
   * See [PagedList.BoundaryCallback.onItemAtEndLoaded]
   */
  override fun onItemAtEndLoaded(itemAtEnd: Data) {
    /**
     * If a content update or delete request is in progress, we will skip loading from network
     */
    if (boundaryCallbackNotifier.hasRequests()) {
      updateUiState(UiStateManager.UiState.LOADED)
      return
    }
    /**
     * Once content is update/deleted the page number should be pageReset,
     * as server will have the latest data
     */
    if (boundaryCallbackNotifier.shouldReset()) {
      updatePageNumber(0)
    }
    updateUiState(UiStateManager.UiState.LOADING)
    updateCallbackType(PagedBoundaryCallback.BoundaryCallbackType.APPENDING)
    requestAndSaveBookmarks()
  }

  private fun requestAndSaveBookmarks() {
    /**
     * Run only single request at a time
     */
    if (isRunning()) return

    threadManager.networkExecutor.execute {
      updateRunning(true)
      val (bookmarks, nextPage, isSuccessFul) = getBookmarks()
      if (isSuccessFul) {
        saveBookmarks(bookmarks)
        handleSuccess()
      }
      updatePageNumber(nextPage)
      updateRunning()
    }
  }

  private fun getBookmarks(): Triple<List<Data>?, Int?, Boolean> {
    // If page number is null, we have loaded all the pages.
    val pageNumber = pageNumber() ?: return Triple(null, null, true)

    val bookmarkResponse = try {
      bookmarkApi.getBookmarks(pageNumber, NETWORK_PAGE_SIZE).execute()
    } catch (exception: IOException) {
      null
    } catch (exception: RuntimeException) {
      null
    }

    if (null == bookmarkResponse || !bookmarkResponse.isSuccessful) {
      handleError()
      return Triple(null, 0, false)
    }

    val contentBody = bookmarkResponse.body()
    if (contentBody == null) {
      handleError()
      return Triple(null, 0, false)
    }

    val items = contentBody.datum.mapNotNull {
      val dataId = it.getContentId()
      val data = contentBody.included?.first { (id) -> id == dataId }
      data?.addRelationships(listOf(it))
    }

    if (items.isNullOrEmpty()) {
      handleEmpty()
      return Triple(null, 0, false)
    }

    return Triple(items, contentBody.getNextPage(), true)
  }

  private fun saveBookmarks(bookmarks: List<Data>?) {
    if (bookmarks.isNullOrEmpty()) {
      return
    }
    contentLocalDataSource.insertContents(DataType.Bookmarks, bookmarks)
  }
}
