package com.raywenderlich.emitron.data.progressions

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.raywenderlich.emitron.data.content.ContentDataSourceLocal
import com.raywenderlich.emitron.model.CompletionStatus
import com.raywenderlich.emitron.model.Content
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.model.isCompleted
import com.raywenderlich.emitron.utils.BoundaryCallbackNotifier
import com.raywenderlich.emitron.utils.LocalPagedResponse
import com.raywenderlich.emitron.utils.PagedBoundaryCallbackImpl
import com.raywenderlich.emitron.utils.PagedResponse
import com.raywenderlich.emitron.utils.async.ThreadManager
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Repository for progression operations
 */
class ProgressionRepository @Inject constructor(
  private val api: ProgressionApi,
  private val threadManager: ThreadManager,
  private val contentDataSourceLocal: ContentDataSourceLocal
) {

  companion object {
    const val PAGE_SIZE: Int = 10
  }

  /**
   * Create/Update a progression
   *
   * @param contentId Content id for progression to be created/updated
   *
   * @return Pair of response [Content] and True/False if request was succeeded/failed
   */
  @WorkerThread
  @Throws(Exception::class)
  suspend fun updateProgression(contentId: String): Pair<Content?, Boolean> {
    val progression = Content.newProgression(contentId)
    return withContext(threadManager.io) {
      contentDataSourceLocal.updateProgress(contentId, true)
      val response = api.createProgression(progression)
      response.body() to response.isSuccessful
    }
  }

  /**
   * Delete a progression
   *
   * @param progressionId progression id to be deleted
   *
   * @return True, if request was successful, else False
   */
  @WorkerThread
  @Throws(Exception::class)
  suspend fun deleteProgression(progressionId: String): Boolean {
    return withContext(threadManager.io) {
      val response = api.deleteProgression(progressionId)
      response.isSuccessful
    }
  }

  /**
   * Update bookmark id for content id
   *
   * @param contentId Content id
   * @param finished mark content completed
   */
  @WorkerThread
  @Throws(Exception::class)
  suspend fun updateProgressionInDb(contentId: String, finished: Boolean = false) {
    return withContext(threadManager.io) {
      contentDataSourceLocal.updateProgress(contentId, finished)
    }
  }

  /**
   * Fetch progressions
   *
   * @param completionStatus Progression completion state [CompletionStatus]
   * @param pageSize Default page size to be fetched
   *
   * @return [PagedResponse] containing LiveData objects of network state,
   * initial meta data, retry callback and paged list
   */
  @MainThread
  fun getProgressions(
    completionStatus: CompletionStatus,
    boundaryCallbackNotifier: BoundaryCallbackNotifier? = null
  ): LocalPagedResponse<Data> {

    val sourceFactory =
      contentDataSourceLocal.getProgressions(completionStatus.isCompleted()).map { it.toData() }

    val pagedListConfig = PagedList.Config.Builder()
      .setInitialLoadSizeHint(PAGE_SIZE)
      .setPageSize(PAGE_SIZE)
      .build()

    val boundaryCallback =
      ProgressionBoundaryCallback(
        api,
        contentDataSourceLocal,
        completionStatus,
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
      networkState = boundaryCallback.networkState()
    )
  }
}
