package com.razeware.emitron.data.progressions

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.razeware.emitron.data.content.ContentDataSourceLocal
import com.razeware.emitron.model.*
import com.razeware.emitron.model.entity.ContentDetail
import com.razeware.emitron.model.entity.Progression
import com.razeware.emitron.model.entity.WatchStat
import com.razeware.emitron.utils.BoundaryCallbackNotifier
import com.razeware.emitron.utils.LocalPagedResponse
import com.razeware.emitron.utils.PagedBoundaryCallbackImpl
import com.razeware.emitron.utils.PagedResponse
import com.razeware.emitron.utils.async.ThreadManager
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
  suspend fun updateProgressions(
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
      uiState = boundaryCallback.uiState()
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
   * @param progressions New/Updated progressions[Contents]
   *
   * @return [Contents]
   */
  @WorkerThread
  @Throws(Exception::class)
  suspend fun updateProgressions(progressions: Contents): Contents? {
    return withContext(threadManager.io) {
      api.updateProgression(progressions)
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

  /**
   * Update offline watch stat
   *
   * @param contentId Content id
   * @param duration Long
   * @param watchedAt Content watched at time
   */
  @WorkerThread
  @Throws(Exception::class)
  suspend fun updateWatchStat(
    contentId: String,
    duration: Long,
    watchedAt: LocalDateTime
  ) {
    return withContext(threadManager.io) {
      progressionDataSource.updateWatchStat(contentId, duration, watchedAt)
    }
  }

  /**
   * Get watch stats
   */
  suspend fun getWatchStats(): List<WatchStat> {
    return withContext(threadManager.db) {
      progressionDataSource.getWatchStats()
    }
  }

  /**
   * Create/Update a watch stats
   *
   * @param watchStats New/Updated WatchStats [Contents]
   *
   * @return [Contents]
   */
  @WorkerThread
  @Throws(Exception::class)
  suspend fun updateWatchStats(
    watchStats: Contents
  ): Response<Contents?> {
    return withContext(threadManager.io) {
      api.updateWatchStats(watchStats)
    }
  }

  /**
   * Delete all watch stats
   */
  @WorkerThread
  @Throws(Exception::class)
  suspend fun deleteWatchStats() {
    return withContext(threadManager.db) {
      progressionDataSource.deleteWatchStats()
    }
  }

  /**
   * Fetches the contents by an id.
   * */
  suspend fun getContent(contentId: String): ContentDetail? {
    return contentDataSourceLocal.getContent(contentId)
  }
}
