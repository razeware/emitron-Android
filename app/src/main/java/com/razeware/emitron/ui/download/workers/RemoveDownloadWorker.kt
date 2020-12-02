package com.razeware.emitron.ui.download.workers

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.*
import com.razeware.emitron.data.download.DownloadRepository
import com.razeware.emitron.ui.download.DownloadService
import com.razeware.emitron.ui.download.workers.StartDownloadWorker.Companion.DOWNLOAD_EPISODE_ID

/**
 * Worker for stopping downloads
 */
class RemoveDownloadWorker @WorkerInject constructor(
  @Assisted val appContext: Context,
  @Assisted workerParameters: WorkerParameters,
  /**
   * Download repository
   */
  val downloadRepository: DownloadRepository
) : CoroutineWorker(appContext, workerParameters) {

  /**
   * See [Worker.doWork]
   */
  override suspend fun doWork(): Result {
    val contentId = inputData.getString(DOWNLOAD_CONTENT_ID)

    val episodeId = inputData.getString(DOWNLOAD_EPISODE_ID)

    val downloadId = episodeId ?: contentId


    if (downloadId.isNullOrBlank()) {
      downloadRepository.removeAllDownloads()
      DownloadService.removeAllDownloads(appContext)
    } else {

      val download =
        downloadRepository.getDownload(downloadId)

      if (null != download) {
        // Remove download from db
        val downloadIds = download.getDownloadIds()
        downloadRepository.removeDownload(downloadIds)
        // Remove download from storage

        downloadIds.map { id ->
          DownloadService.removeDownload(appContext, id)
        }
      }
    }


    return Result.success()
  }

  companion object {

    /**
     * Content id to be downloaded
     */
    const val DOWNLOAD_CONTENT_ID: String = "download_content_id"


    private const val DOWNLOAD_WORKER_TAG: String = "downloads"

    /**
     * Stop content download
     *
     * @param workManager WorkManager
     * @param contentId
     * @param episodeId
     */
    fun enqueue(
      workManager: WorkManager,
      contentId: String? = null,
      episodeId: String? = null
    ) {
      val downloadData =
        workDataOf(
          DOWNLOAD_CONTENT_ID to contentId,
          DOWNLOAD_EPISODE_ID to episodeId
        )

      val workRequest = OneTimeWorkRequestBuilder<RemoveDownloadWorker>()
        .setInputData(downloadData)
        .addTag(DOWNLOAD_WORKER_TAG)
        .build()

      val downloadId = episodeId ?: contentId

      if (downloadId.isNullOrBlank()) {
        workManager.enqueue(workRequest)
      } else {
        workManager
          .enqueueUniqueWork(
            downloadId,
            ExistingWorkPolicy.REPLACE,
            workRequest
          )
      }
    }
  }
}
