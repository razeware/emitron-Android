package com.razeware.emitron.data.progressions

import androidx.paging.PagedList
import com.razeware.emitron.data.content.ContentDataSourceLocal
import com.razeware.emitron.model.CompletionStatus
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.DataType
import com.razeware.emitron.utils.*
import com.razeware.emitron.utils.async.ThreadManager
import java.io.IOException

/**
 * Progressions boundary callback to fetch progressions when no data is in database,
 * or last item of database has been queried.
 */
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
    /**
     * The notifier has pending requests, and the boundary callback starts from zero items,
     * surely the last item in paging library was updated/edited, let's send empty response,
     * as once the request finishes, the DB will be updated again.
     */
    if (boundaryCallbackNotifier.hasRequests()) {
      updateUiState(UiStateManager.UiState.INIT_EMPTY)
      return
    }
    /**
     * The notifier requests callback to start from page 0. This will happen when we have already
     * assigned a boundary callback and we are requesting a certain page, but then paging list is
     * updated/changed from db.
     */
    if (boundaryCallbackNotifier.shouldReset()) {
      updatePageNumber(0)
    }
    updateUiState(UiStateManager.UiState.INIT)
    updateCallbackType(PagedBoundaryCallback.BoundaryCallbackType.INIT)
    requestAndSaveProgressions()
  }

  /**
   * See [PagedList.BoundaryCallback.onItemAtEndLoaded]
   */
  override fun onItemAtEndLoaded(itemAtEnd: Data) {
    if (boundaryCallbackNotifier.hasRequests()) {
      updateUiState(UiStateManager.UiState.LOADED)
      return
    }
    if (boundaryCallbackNotifier.shouldReset()) {
      updatePageNumber(0)
    }
    updateUiState(UiStateManager.UiState.LOADING)
    updateCallbackType(PagedBoundaryCallback.BoundaryCallbackType.APPENDING)
    requestAndSaveProgressions()
  }

  private fun requestAndSaveProgressions() {
    if (isRunning()) return

    threadManager.networkExecutor.execute {
      updateRunning(true)
      val (progressions, nextPage, isSuccessFul) = getProgressions()
      if (isSuccessFul) {
        saveProgressions(progressions)
        handleSuccess()
      }
      updatePageNumber(nextPage)
      updateRunning()
    }
  }

  private fun getProgressions(): Triple<List<Data>, Int?, Boolean> {
    val pageNumber = pageNumber()
    if (pageNumber == null) {
      handleEmpty()
      return Triple(emptyList(), null, false)
    }

    val progressionsResponse = try {
      progressionApi.getProgressions(
        pageNumber,
        NETWORK_PAGE_SIZE,
        completionStatus = completionStatus.param
      ).execute()

    } catch (exception: IOException) {
      null
    } catch (exception: RuntimeException) {
      null
    }

    if (null == progressionsResponse || !progressionsResponse.isSuccessful) {
      handleError()
      return Triple(emptyList(), 0, false)
    }

    val contentBody = progressionsResponse.body()
    if (contentBody == null) {
      handleError()
      return Triple(emptyList(), 0, false)
    }


    val items = contentBody.datum.mapNotNull {
      val dataId = it.getContentId()
      /**
       * Map each progression object to included content object,
       * also update the content object relations with progression object
       */
      val data = contentBody.included?.first { (id) -> id == dataId }
      data?.updateRelationships(listOf(it))
    }

    if (items.isNullOrEmpty()) {
      handleEmpty()
      return Triple(emptyList(), 0, false)
    }

    return Triple(items, contentBody.getNextPage(), true)
  }

  private fun saveProgressions(progressions: List<Data>) {
    contentLocalDataSource.insertContents(DataType.Progressions, progressions)
  }
}
