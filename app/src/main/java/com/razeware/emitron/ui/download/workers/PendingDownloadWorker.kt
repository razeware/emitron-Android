package com.razeware.emitron.ui.download.workers

import android.content.Context
import androidx.work.*
import com.razeware.emitron.data.download.DownloadRepository
import com.razeware.emitron.di.modules.worker.ChildWorkerFactory
import com.razeware.emitron.model.ContentType
import com.razeware.emitron.model.DownloadState
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

    private const val DOWNLOAD_WORKER_NAME: String = "pending_downloads"

    /**
     * Queue verify download worker
     *
     * @param workManager WorkManager
     */
    fun enqueue(workManager: WorkManager, downloadOnlyOnWifi: Boolean) {

      val updatePendingDownloads = OneTimeWorkRequestBuilder<UpdateDownloadWorker>()
        .build()

      val downloadWorkRequest =
        DownloadWorker.buildWorkRequest(downloadOnlyOnWifi)

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
