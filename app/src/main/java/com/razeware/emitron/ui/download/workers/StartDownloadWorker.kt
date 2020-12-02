package com.razeware.emitron.ui.download.workers

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.*
import com.razeware.emitron.data.download.DownloadRepository
import com.razeware.emitron.model.entity.Download
import com.razeware.emitron.model.entity.inProgress
import com.razeware.emitron.model.entity.isCompleted
import com.razeware.emitron.model.entity.isPaused
import com.razeware.emitron.ui.download.DownloadService

/**
 *  Worker for starting a download,
 *
 * It will fetch content to be downloaded and add it to db.
 * It will be followed by [DownloadWorker] which will read from database and forward downloads
 * to [DownloadService]
 */
class StartDownloadWorker @WorkerInject constructor(
  @Assisted appContext: Context,
  @Assisted workerParameters: WorkerParameters,
  /** Settings Repository */
  val downloadRepository: DownloadRepository
) : CoroutineWorker(appContext, workerParameters) {

  /**
   * See [Worker.doWork]
   */
  override suspend fun doWork(): Result {
    val contentId =
      inputData.getString(DOWNLOAD_CONTENT_ID) ?: return Result.failure()

    val episodeId = inputData.getString(DOWNLOAD_EPISODE_ID)

    val downloadId = episodeId ?: contentId

    // Check db download state
    val download =
      downloadRepository.getDownload(downloadId)

    return when {
      download.inProgress() -> {
        val outputData = workDataOf(
          DownloadWorker.DOWNLOAD_ID to download?.getDownloadId()
        )
        Result.success(outputData)
      }
      download.isPaused() -> {
        val outputData = workDataOf(
          DownloadWorker.DOWNLOAD_ID to download?.getDownloadId()
        )
        Result.success(outputData)
      }
      download.isCompleted() -> {
        // Yay!
        Result.success()
      }
      else -> {
        addDownload(contentId, episodeId)
      }
    }
  }

  private suspend fun addDownload(contentId: String, episodeId: String?): Result {
    // Save content to db
    val content = downloadRepository.fetchAndSaveContent(contentId)
    content ?: return Result.failure()

    val downloadIds: List<String> = when {
      // If content is screencast simply add it's id
      content.isTypeScreencast() -> {
        listOf(contentId)
      }
      // If episode id isn't null, that implies the user is only downloading a single episode
      null != episodeId -> {
        listOf(episodeId).plus(contentId)
      }
      // Episode id is null also content type is collection
      content.isTypeCollection() -> {
        content.getEpisodeIds().plus(contentId)
      }
      else -> {
        // Aww! snap!
        emptyList()
      }
    }
    // Queue download for next worker
    if (downloadIds.isNotEmpty()) {
      // Let's queue the download list
      val downloads = downloadIds.map { downloadId ->
        Download.with(downloadId)
      }
      downloadRepository.addDownloads(downloads)
    }

    return Result.success()
  }

  companion object {

    /**
     * Content id to be downloaded
     */
    const val DOWNLOAD_CONTENT_ID: String = "download_content_id"

    /**
     * Episode id to be downloaded
     */
    const val DOWNLOAD_EPISODE_ID: String = "download_episode_id"

    private const val DOWNLOAD_WORKER_TAG: String = "downloads"

    /**
     * Start content download
     *
     * @param workManager WorkManager
     * @param contentId
     * @param episodeId
     */
    fun enqueue(
      workManager: WorkManager,
      contentId: String,
      episodeId: String? = null,
      downloadOnlyOnWifi: Boolean
    ) {
      val downloadData =
        workDataOf(
          DOWNLOAD_CONTENT_ID to contentId,
          DOWNLOAD_EPISODE_ID to episodeId
        )

      val startDownloadWorkRequest = OneTimeWorkRequestBuilder<StartDownloadWorker>()
        .setInputData(downloadData)
        .addTag(DOWNLOAD_WORKER_TAG)
        .build()

      val downloadWorkRequest =
        DownloadWorker.buildWorkRequest(downloadOnlyOnWifi)

      workManager
        .beginUniqueWork(
          contentId,
          ExistingWorkPolicy.REPLACE,
          startDownloadWorkRequest
        )
        .then(downloadWorkRequest)
        .enqueue()
    }
  }
}
