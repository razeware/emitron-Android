package com.raywenderlich.emitron.data.download

import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.raywenderlich.emitron.data.content.ContentDataSourceLocal
import com.raywenderlich.emitron.model.Content
import com.raywenderlich.emitron.model.Contents
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.model.DownloadState
import com.raywenderlich.emitron.model.entity.Download
import com.raywenderlich.emitron.model.entity.DownloadWithContent
import com.raywenderlich.emitron.utils.LocalPagedResponse
import com.raywenderlich.emitron.utils.PagedBoundaryCallbackImpl
import com.raywenderlich.emitron.utils.PagedResponse
import com.raywenderlich.emitron.utils.async.ThreadManager
import kotlinx.coroutines.withContext
import org.threeten.bp.Clock
import org.threeten.bp.LocalDateTime
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * Repository for video playback actions
 */
class DownloadRepository @Inject constructor(
  private val downloadApi: DownloadApi,
  private val threadManager: ThreadManager,
  private val contentDataSourceLocal: ContentDataSourceLocal
) {

  companion object {
    /**
     * Download page size
     */
    const val PAGE_SIZE: Int = 10
  }

  /**
   * Get content
   *
   * @param id Content id to be fetched
   *
   * @return [Content] Response Content
   */
  @WorkerThread
  suspend fun fetchAndSaveContent(id: String): Content? {
    return withContext(threadManager.io) {
      val content = try {
        downloadApi.getContent(id)
      } catch (exception: IOException) {
        null
      } catch (exception: HttpException) {
        null
      }
      if (null != content) {
        contentDataSourceLocal.insertContent(content)
      }
      content
    }
  }

  /**
   * Get queued downloads
   *
   * @param limit no. of pending downloads to fetch
   * @param states download states
   * @param contentTypes download contentTypes
   */
  @AnyThread
  suspend fun getQueuedDownloads(
    limit: Int,
    states: Array<DownloadState>,
    contentTypes: Array<String>
  ): List<DownloadWithContent> {
    return withContext(threadManager.db) {
      contentDataSourceLocal.getQueuedDownloads(
        limit,
        states.map { it.ordinal }.toTypedArray(),
        contentTypes
      )
    }
  }

  /**
   * Get queued download by id
   *
   * @param downloadId Download id
   */
  @AnyThread
  suspend fun getDownload(downloadId: String): DownloadWithContent? {
    return withContext(threadManager.db) {
      contentDataSourceLocal.getDownload(downloadId)
    }
  }

  /**
   * Add new download
   *
   * @param downloadId Content id
   * @param downloadState Download state
   */
  @AnyThread
  suspend fun addDownload(
    downloadId: String, downloadState: DownloadState,
    createdAt: LocalDateTime = LocalDateTime.now(Clock.systemUTC())
  ) {
    withContext(threadManager.db) {
      contentDataSourceLocal.insertDownload(downloadId, downloadState, createdAt)
    }
  }

  /**
   * Remove a download
   *
   * @param downloadId Download id
   */
  @AnyThread
  suspend fun removeDownload(downloadIds: List<String>) {
    withContext(threadManager.db) {
      contentDataSourceLocal.deleteDownload(downloadIds)
    }
  }

  /**
   * Delete all downloads
   */
  @WorkerThread
  suspend fun removeAllDownloads() {
    withContext(threadManager.db) {
      contentDataSourceLocal.deleteAllDownloads()
    }
  }

  /**
   * Get download url
   *
   * @param id Video id
   */
  @WorkerThread
  suspend fun getDownloadUrl(id: String): Contents? {
    return withContext(threadManager.io) {
      try {
        downloadApi.getDownloadUrl(id)
      } catch (exception: IOException) {
        null
      } catch (exception: RuntimeException) {
        null
      }
    }
  }


  /**
   * Update content download url
   *
   * @param contentId Content id
   * @param url Download url
   */
  @AnyThread
  suspend fun updateDownloadUrl(contentId: String, url: String) {
    withContext(threadManager.db) {
      contentDataSourceLocal
        .updateDownloadUrl(contentId, url)
    }
  }

  /**
   * Update download progress
   *
   * @param contentId Content id
   * @param progress Int
   * @param state Download state [DownloadState]
   */
  @AnyThread
  suspend fun updateDownloadProgress(
    contentId: String,
    progress: Int,
    state: DownloadState
  ) {
    withContext(threadManager.db) {
      contentDataSourceLocal.updateDownloadProgress(contentId, progress, state)
    }
  }

  /**
   * Update download state
   *
   * @param contentId Content id
   * @param state Download state [DownloadState]
   */
  @AnyThread
  suspend fun updateDownloadState(
    contentId: String,
    state: DownloadState
  ) {
    withContext(threadManager.db) {
      contentDataSourceLocal.updateDownloadState(contentId, state)
    }
  }

  /**
   * Fetch bookmarks
   *
   * @return [PagedResponse] containing LiveData objects of network state,
   * initial meta data, retry callback and paged list
   */
  @MainThread
  fun getDownloads(): LocalPagedResponse<Data> {

    val sourceFactory =
      contentDataSourceLocal.getQueuedDownloads().map { it.toData() }

    val boundaryCallback =
      DownloadBoundaryCallback(PagedBoundaryCallbackImpl())

    val pagedListConfig = PagedList.Config.Builder()
      .setInitialLoadSizeHint(PAGE_SIZE)
      .setPageSize(PAGE_SIZE)
      .build()

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
   * Get downloads by id
   *
   * @param ids Download ids
   */
  fun getDownloadsById(ids: List<String>): LiveData<List<Download>> =
    contentDataSourceLocal.getDownloadsById(ids)
}
