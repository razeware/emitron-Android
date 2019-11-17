package com.raywenderlich.emitron.ui.download.workers

import android.content.Context
import androidx.work.*
import com.raywenderlich.emitron.data.download.DownloadRepository
import com.raywenderlich.emitron.di.modules.worker.ChildWorkerFactory
import com.raywenderlich.emitron.model.ContentType
import com.raywenderlich.emitron.model.DownloadState
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

/**
 *  Worker for handling failed downloads on app start.
 *
 */
class PendingDownloadWorker @AssistedInject constructor(
  @Assisted private val appContext: Context,
  @Assisted private val workerParameters: WorkerParameters,
  private val downloadManager: com.google.android.exoplayer2.offline.DownloadManager,
  private val downloadRepository: DownloadRepository
) : CoroutineWorker(appContext, workerParameters) {

  /**
   * See [Worker.doWork]
   */
  override suspend fun doWork(): Result {
    val inProgressDownloadsFromDb =
      downloadRepository.getInProgressDownloads(
        contentTypes = ContentType.getAllowedDownloadTypes()
      ).mapNotNull { it.getContentId() }
    val inProgressDownloads = downloadManager.currentDownloads

    if (!inProgressDownloads.isNullOrEmpty()) {
      val downloadIds = inProgressDownloadsFromDb.subtract(
        inProgressDownloads.mapNotNull {
          it.request.id
        })

      if (downloadIds.isNotEmpty()) {
        downloadRepository.updateDownloadState(downloadIds.toList(), state = DownloadState.CREATED)
      }
    }
    return Result.success()
  }

  /**
   * [PendingDownloadWorker.Factory]
   */
  @AssistedInject.Factory
  interface Factory : ChildWorkerFactory

  companion object {

    private const val DOWNLOAD_WORKER_TAG: String = "failed_downloads"
    private const val DOWNLOAD_WORKER_NAME: String = "failed_download"

    /**
     * Queue verify download worker
     *
     * @param workManager WorkManager
     */
    fun queue(workManager: WorkManager) {

      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.UNMETERED)
        .setRequiresStorageNotLow(true)
        .build()

      val updatePendingDownloads = OneTimeWorkRequestBuilder<UpdateDownloadWorker>()
        .addTag(DOWNLOAD_WORKER_TAG)
        .build()

      val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
        .setConstraints(constraints)
        .addTag(DOWNLOAD_WORKER_TAG)
        .build()

      workManager
        .beginUniqueWork(
          DOWNLOAD_WORKER_NAME,
          ExistingWorkPolicy.REPLACE,
          updatePendingDownloads
        )
        .then(downloadWorkRequest)
        .enqueue()
    }
  }
}
