package com.raywenderlich.emitron.data.progressions

import androidx.paging.PagedList
import com.raywenderlich.emitron.data.content.ContentDataSourceLocal
import com.raywenderlich.emitron.model.CompletionStatus
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.model.DataType
import com.raywenderlich.emitron.utils.*
import com.raywenderlich.emitron.utils.async.ThreadManager

class ProgressionBoundaryCallback(
  private val progressionApi: ProgressionApi,
  private val contentLocalDataSource: ContentDataSourceLocal,
  private val completionStatus: CompletionStatus,
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
    if (boundaryCallbackNotifier.hasRequests()) {
      updateNetworkState(NetworkState.INIT_EMPTY)
      return
    }
    if (boundaryCallbackNotifier.shouldReset()) {
      updatePageNumber(0)
    }
    updateNetworkState(NetworkState.INIT)
    updateCallbackType(PagedBoundaryCallback.BoundaryCallbackType.INIT)
    requestAndSaveBookmarks()
  }

  /**
   * See [PagedList.BoundaryCallback.onItemAtEndLoaded]
   */
  override fun onItemAtEndLoaded(itemAtEnd: Data) {
    if (boundaryCallbackNotifier.hasRequests()) {
      updateNetworkState(NetworkState.SUCCESS)
      return
    }
    if (boundaryCallbackNotifier.shouldReset()) {
      updatePageNumber(0)
    }
    updateNetworkState(NetworkState.RUNNING)
    updateCallbackType(PagedBoundaryCallback.BoundaryCallbackType.APPENDING)
    requestAndSaveBookmarks()
  }

  private fun requestAndSaveBookmarks() {
    if (isRunning()) return

    threadManager.networkExecutor.execute {
      updateRunning(true)
      val (bookmarks, nextPage, isSuccessFul) = getProgressions()
      if (isSuccessFul) {
        saveBookmarks(bookmarks)
        handleSuccess()
      }
      updatePageNumber(nextPage)
      updateRunning()
    }
  }

  private fun getProgressions(): Triple<List<Data>?, Int?, Boolean> {
    val pageNumber = pageNumber() ?: return Triple(null, null, true)
    val bookmarkResponse =
      progressionApi.getProgressions(
        pageNumber,
        NETWORK_PAGE_SIZE,
        completionStatus = completionStatus.param
      ).execute()

    if (!bookmarkResponse.isSuccessful) {
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
      data?.updateRelationships(listOf(it))
    }

    if (items.isNullOrEmpty()) {
      handleEmpty()
      return Triple(null, 0, false)
    }

    return Triple(items, contentBody.getNextPage(), true)
  }

  private fun saveBookmarks(progressions: List<Data>?) {
    if (progressions.isNullOrEmpty()) {
      handleEmpty()
      return
    }
    contentLocalDataSource.insertContent(DataType.Progressions, progressions)
  }
}
