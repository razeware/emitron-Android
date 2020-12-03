package com.razeware.emitron.ui.download.workers

import android.content.Context
import android.net.Uri
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.*
import com.razeware.emitron.data.download.DownloadRepository
import com.razeware.emitron.data.settings.SettingsRepository
import com.razeware.emitron.model.ContentType
import com.razeware.emitron.model.DownloadQuality
import com.razeware.emitron.model.DownloadState
import com.razeware.emitron.model.entity.inProgress
import com.razeware.emitron.model.entity.isPaused
import com.razeware.emitron.model.isHd
import com.razeware.emitron.ui.download.DownloadService

/**
 * Worker for processing queued Downloads
 */
class DownloadWorker @WorkerInject constructor(
  @Assisted val appContext: Context,
  @Assisted workerParameters: WorkerParameters,
  /**
   * Download repository
   */
  val downloadRepository: DownloadRepository,
  /**
   * Settings repository
   */
  val settingsRepository: SettingsRepository
) : CoroutineWorker(appContext, workerParameters) {

  /**
   * Get all queued downloads and add to them to [DownloadService]
   * See [Worker.doWork]
   */
  override suspend fun doWork(): Result {

    val downloadId = inputData.getString(DOWNLOAD_ID)

    if (null != downloadId) {
      handleExistingDownload(downloadId)
    } else {
      handleQueuedDownloads()
    }

    return Result.success()
  }

  private suspend fun handleExistingDownload(downloadId: String): Result {
    val download =
      downloadRepository.getDownload(downloadId)
    return when {
      download.inProgress() -> {
        downloadRepository.updateDownloadState(listOf(downloadId), DownloadState.PAUSED)
        DownloadService.pauseDownload(appContext, downloadId)
        handleQueuedDownloads()
      }
      download.isPaused() -> {
        downloadRepository.updateDownloadState(listOf(downloadId), DownloadState.IN_PROGRESS)
        DownloadService.resumeDownload(appContext, downloadId)
        Result.success()
      }
      else -> {
        Result.success()
      }
    }
  }

  private suspend fun handleQueuedDownloads(): Result {
    // Get all queued downloads
    val queuedDownloads =
      downloadRepository.getQueuedDownloads(
        limit = MAX_PARALLEL_DOWNLOADS,
        states = arrayOf(DownloadState.CREATED),
        contentTypes = ContentType.getAllowedDownloadTypes()
      )

    // Get download urls for queued downloads

    queuedDownloads.map { download ->
      val downloadRequest = createDownloadRequest(
        download.getContentId(),
        download.getVideoId()
      )
      if (null != downloadRequest) {
        downloadRepository.updateDownloadUrl(
          downloadRequest.contentId,
          downloadRequest.downloadUrl
        )
        DownloadService.startDownload(
          appContext,
          downloadRequest.contentId,
          Uri.parse(downloadRequest.downloadUrl),
          download.getContentName()
        )
      } else {
        return Result.failure()
      }
    }

    return Result.success()
  }

  private suspend fun createDownloadRequest(
    contentId: String?,
    videoId: String?
  ): DownloadRequest? {
    contentId ?: return null
    videoId ?: return null

    val contents = downloadRepository.getDownloadUrl(videoId)
    contents ?: return null

    val downloadQuality = DownloadQuality.fromPref(settingsRepository.getDownloadQuality())

    val downloadUrl = contents.getDownloadUrl(downloadQuality.isHd())

    return if (!downloadUrl.isNullOrEmpty()) {
      DownloadRequest(
        contentId,
        downloadUrl
      )
    } else {
      // update failure reason
      null
    }
  }

  internal data class DownloadRequest(val contentId: String, val downloadUrl: String)

  companion object {
    private const val MAX_PARALLEL_DOWNLOADS: Int = 1

    /**
     * Download id
     */
    const val DOWNLOAD_ID: String = "download_id"

    private const val DOWNLOAD_WORKER_TAG: String = "downloads"

    /**
     * Build Download work request
     */
    fun buildWorkRequest(downloadOnlyOnWifi: Boolean = false): OneTimeWorkRequest {
      val networkType = if (downloadOnlyOnWifi) {
        NetworkType.UNMETERED
      } else {
        NetworkType.CONNECTED
      }
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(networkType)
        .setRequiresStorageNotLow(true)
        .build()

      return OneTimeWorkRequestBuilder<DownloadWorker>()
        .setConstraints(constraints)
        .addTag(DOWNLOAD_WORKER_TAG)
        .build()
    }
  }
}
