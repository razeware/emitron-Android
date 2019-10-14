package com.raywenderlich.emitron.ui.download.workers

import android.content.Context
import androidx.work.*
import com.raywenderlich.emitron.data.download.DownloadRepository
import com.raywenderlich.emitron.di.modules.worker.ChildWorkerFactory
import com.raywenderlich.emitron.model.DownloadState
import com.raywenderlich.emitron.model.entity.inProgress
import com.raywenderlich.emitron.model.entity.isCompleted
import com.raywenderlich.emitron.model.entity.isPaused
import com.raywenderlich.emitron.ui.download.DownloadService
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

/**
 *  Worker for starting a download,
 *
 * It will fetch content to be downloaded and add it to db.
 * It will be followed by [DownloadWorker] which will read from database and forward downloads
 * to [DownloadService]
 */
class StartDownloadWorker @AssistedInject constructor(
  @Assisted private val appContext: Context,
  @Assisted private val workerParameters: WorkerParameters,
  private val downloadRepository: DownloadRepository
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
        listOf(episodeId)
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
      downloadIds.map { downloadId ->
        downloadRepository.addDownload(downloadId, DownloadState.CREATED)
      }
    }

    return Result.success()
  }

  /**
   * [StartDownloadWorker.Factory]
   */
  @AssistedInject.Factory
  interface Factory : ChildWorkerFactory

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
      episodeId: String? = null
    ) {
      val downloadData =
        workDataOf(
          DOWNLOAD_CONTENT_ID to contentId,
          DOWNLOAD_EPISODE_ID to episodeId
        )

      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.UNMETERED)
        .setRequiresStorageNotLow(true)
        .build()

      val startDownloadWorkRequest = OneTimeWorkRequestBuilder<StartDownloadWorker>()
        .setInputData(downloadData)
        .setConstraints(constraints)
        .addTag(DOWNLOAD_WORKER_TAG)
        .build()

      val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
        .setInputData(downloadData)
        .setConstraints(constraints)
        .addTag(DOWNLOAD_WORKER_TAG)
        .build()

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
