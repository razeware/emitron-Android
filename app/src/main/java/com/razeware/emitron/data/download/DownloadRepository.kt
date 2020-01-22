package com.razeware.emitron.data.download

import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.razeware.emitron.data.content.ContentDataSourceLocal
import com.razeware.emitron.model.*
import com.razeware.emitron.model.entity.Download
import com.razeware.emitron.model.entity.DownloadWithContent
import com.razeware.emitron.utils.LocalPagedResponse
import com.razeware.emitron.utils.PagedBoundaryCallbackImpl
import com.razeware.emitron.utils.PagedResponse
import com.razeware.emitron.utils.async.ThreadManager
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
  private val contentDataSource: ContentDataSourceLocal,
  private val downloadDataSource: DownloadDataSourceLocal
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
        contentDataSource.insertContent(content)
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
      downloadDataSource.getQueuedDownloads(
        limit,
        states.map { it.ordinal }.toTypedArray(),
        contentTypes
      )
    }
  }

  /**
   * Get in progress downloads
   *
   * @param contentTypes
   */
  @AnyThread
  suspend fun getInProgressDownloads(
    contentTypes: Array<String>
  ): List<DownloadWithContent> {
    return withContext(threadManager.db) {
      downloadDataSource.getInProgressDownloads(
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
      downloadDataSource.getDownload(downloadId)
    }
  }

  /**
   * Add Downloads
   */
  suspend fun addDownloads(downloads: List<Download>) {
    return withContext(threadManager.db) {
      downloadDataSource.addDownloads(downloads)
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
    downloadId: String,
    downloadState: DownloadState,
    createdAt: LocalDateTime = LocalDateTime.now(Clock.systemUTC())
  ) {
    withContext(threadManager.db) {
      downloadDataSource.insertDownload(downloadId, downloadState, createdAt)
    }
  }

  /**
   * Remove a download
   *
   * @param downloadIds List of Download id
   */
  @AnyThread
  suspend fun removeDownload(downloadIds: List<String>) {
    withContext(threadManager.db) {
      downloadDataSource.deleteDownload(downloadIds)
    }
  }

  /**
   * Delete all downloads
   */
  @WorkerThread
  suspend fun removeAllDownloads() {
    withContext(threadManager.db) {
      downloadDataSource.deleteAllDownloads()
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
      downloadDataSource
        .updateDownloadUrl(contentId, url)
    }
  }

  /**
   * Update download progress
   *
   * @param progress [DownloadProgress]
   */
  @AnyThread
  suspend fun updateDownloadProgress(progress: DownloadProgress) {
    withContext(threadManager.db) {
      downloadDataSource.updateDownloadProgress(progress)
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
    contentId: List<String>,
    state: DownloadState
  ) {
    withContext(threadManager.db) {
      downloadDataSource.updateDownloadState(contentId, state)
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
      downloadDataSource.getQueuedDownloads().map { it.toData() }

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
      uiState = boundaryCallback.uiState()
    )
  }


  /**
   * Get downloads by id
   *
   * @param ids Download ids
   */
  fun getDownloadsById(ids: List<String>): LiveData<List<Download>> =
    downloadDataSource.getDownloadsById(ids)
}
