package com.raywenderlich.emitron.ui.download.workers

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.raywenderlich.emitron.data.download.DownloadRepository
import com.raywenderlich.emitron.data.settings.SettingsRepository
import com.raywenderlich.emitron.di.modules.worker.ChildWorkerFactory
import com.raywenderlich.emitron.model.ContentType
import com.raywenderlich.emitron.model.DownloadQuality
import com.raywenderlich.emitron.model.DownloadState
import com.raywenderlich.emitron.model.entity.isPaused
import com.raywenderlich.emitron.model.isHd
import com.raywenderlich.emitron.ui.download.DownloadService
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

/**
 * Worker for processing queued Downloads
 */
class DownloadWorker @AssistedInject constructor(
  @Assisted private val appContext: Context,
  @Assisted private val workerParameters: WorkerParameters,
  private val downloadRepository: DownloadRepository,
  private val settingsRepository: SettingsRepository
) : CoroutineWorker(appContext, workerParameters) {

  /**
   * Get all queued downloads and add to them to [DownloadService]
   * See [Worker.doWork]
   */
  override suspend fun doWork(): Result {

    // Get all queued downloads
    val queuedDownloads =
      downloadRepository.getQueuedDownloads(
        limit = MAX_PARALLEL_DOWNLOADS,
        states = arrayOf(DownloadState.CREATED, DownloadState.PAUSED),
        contentTypes = ContentType.getAllowedDownloadTypes()
      )

    // Get download urls for queued downloads

    queuedDownloads.map { download ->
      when {
        download.isPaused() -> {
          downloadRepository.updateDownloadState(
            download.getDownloadId(),
            DownloadState.IN_PROGRESS
          )
          DownloadService.resumeDownload(appContext, download.getDownloadId())
        }
        else -> {
          // Start download
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
              Uri.parse(downloadRequest.downloadUrl)
            )
          } else {
            return Result.failure()
          }
        }
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

  /**
   * [DownloadWorker.Factory]
   */
  @AssistedInject.Factory
  interface Factory : ChildWorkerFactory

  companion object {
    private const val MAX_PARALLEL_DOWNLOADS: Int = 1
  }
}