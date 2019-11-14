package com.raywenderlich.emitron.data.progressions

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.raywenderlich.emitron.data.content.ContentDataSourceLocal
import com.raywenderlich.emitron.model.*
import com.raywenderlich.emitron.model.entity.Progression
import com.raywenderlich.emitron.utils.BoundaryCallbackNotifier
import com.raywenderlich.emitron.utils.LocalPagedResponse
import com.raywenderlich.emitron.utils.PagedBoundaryCallbackImpl
import com.raywenderlich.emitron.utils.PagedResponse
import com.raywenderlich.emitron.utils.async.ThreadManager
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDateTime
import retrofit2.Response
import javax.inject.Inject

/**
 * Repository for progression operations
 */
class ProgressionRepository @Inject constructor(
  private val api: ProgressionApi,
  private val threadManager: ThreadManager,
  private val contentDataSourceLocal: ContentDataSourceLocal,
  private val progressionDataSource: ProgressionDataSourceLocal
) {

  companion object {
    /**
     * Progression items per page
     */
    const val PAGE_SIZE: Int = 10
  }

  /**
   * Create/Update a progression
   *
   * @param contentId Content id for progression to be created/updated
   *
   * @return [Contents]
   */
  @WorkerThread
  @Throws(Exception::class)
  suspend fun updateProgression(
    contentId: String,
    finished: Boolean,
    updatedAt: LocalDateTime
  ): Contents? {
    val progression =
      Data.newProgression(contentId, finished, updatedAt = updatedAt)
    return withContext(threadManager.io) {
      api.updateProgression(Contents.from(progression))
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
   * Fetch progressions
   *
   * @param completionStatus Progression completion state [CompletionStatus]
   * @param boundaryCallbackNotifier Boundary callback notifier
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

  /**
   * Update content Playback
   */
  @Throws(Exception::class)
  suspend fun updatePlaybackProgress(
    playbackToken: String,
    contentId: String,
    progress: Long,
    seconds: Long
  ): Response<Content> {
    val playbackProgress = PlaybackProgress(playbackToken, progress, seconds)
    return withContext(threadManager.io) {
      api.updatePlaybackProgress(contentId, playbackProgress)
    }
  }

  /**
   * Create/Update a progression
   *
   * @param updatedProgressions [Contents]
   *
   * @return [Contents]
   */
  @WorkerThread
  @Throws(Exception::class)
  suspend fun updateProgression(
    updatedProgressions: Contents
  ): Contents? {
    return withContext(threadManager.io) {
      api.updateProgression(updatedProgressions)
    }
  }


  /**
   * Get a progression updated offline
   */
  suspend fun getLocalProgressions(): List<Progression> {
    return withContext(threadManager.db) {
      progressionDataSource.getLocalProgressions()
    }
  }

  /**
   * Update local progressions
   */
  suspend fun updateLocalProgressions(progressions: List<Progression>) {
    return withContext(threadManager.db) {
      progressionDataSource.updateLocalProgressions(progressions)
    }
  }

  /**
   * Update content progress
   *
   * @param contentId Content id
   * @param percentComplete Percentage completion
   * @param progress Content progress
   * @param finished Has content finished
   * @param synced Has content synced? False if content was updated offline, else True
   * @param updatedAt Content updated time
   * @param progressionId Progression Id
   */
  suspend fun updateLocalProgression(
    contentId: String,
    percentComplete: Int,
    progress: Long,
    finished: Boolean,
    synced: Boolean = false,
    updatedAt: LocalDateTime,
    progressionId: String? = null
  ) {
    return withContext(threadManager.io) {
      progressionDataSource.updateProgress(
        contentId,
        percentComplete,
        progress,
        finished,
        synced,
        updatedAt,
        progressionId
      )
    }
  }
}
