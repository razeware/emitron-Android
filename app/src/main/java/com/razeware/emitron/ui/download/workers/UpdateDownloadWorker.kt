package com.razeware.emitron.ui.download.workers

import android.content.Context
import androidx.work.*
import com.razeware.emitron.data.download.DownloadRepository
import com.razeware.emitron.di.modules.worker.ChildWorkerFactory
import com.razeware.emitron.model.DownloadProgress
import com.razeware.emitron.model.DownloadState
import com.razeware.emitron.ui.download.DownloadService
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

/**
 *  Worker for updating a download,
 *
 * It will fetch content to be downloaded and add it to db.
 * It will be followed by [DownloadWorker] which will read from database and forward downloads
 * to [DownloadService]
 */
class UpdateDownloadWorker @AssistedInject constructor(
  @Assisted private val appContext: Context,
  @Assisted private val workerParameters: WorkerParameters,
  private val downloadRepository: DownloadRepository
) : CoroutineWorker(appContext, workerParameters) {

  /**
   * See [Worker.doWork]
   */
  override suspend fun doWork(): Result {
    val downloadId =
      inputData.getString(DOWNLOAD_ID) ?: return Result.failure()

    val downloadProgress = inputData.getInt(DOWNLOAD_PROGRESS, 0)

    val downloadState = inputData.getInt(DOWNLOAD_STATE, DownloadState.FAILED.ordinal)

    val progress = DownloadProgress(
      downloadId,
      downloadProgress,
      DownloadState.values()[downloadState]
    )
    downloadRepository.updateDownloadProgress(progress)

    return Result.success()
  }

  /**
   * [UpdateDownloadWorker.Factory]
   */
  @AssistedInject.Factory
  interface Factory : ChildWorkerFactory

  companion object {

    /**
     * Download id
     */
    const val DOWNLOAD_ID: String = "download_id"
    /**
     * Download progress
     */
    const val DOWNLOAD_PROGRESS: String = "download_progress"

    /**
     * Download state
     */
    const val DOWNLOAD_STATE: String = "download_state"

    private const val DOWNLOAD_WORKER_TAG: String = "downloads"

    /**
     * Start content download
     *
     * @param workManager WorkManager
     * @param downloadId
     * @param progress
     * @param state
     */
    fun updateAndStartNext(
      workManager: WorkManager,
      downloadId: String,
      progress: Int,
      state: DownloadState
    ) {
      val downloadData =
        workDataOf(
          DOWNLOAD_ID to downloadId,
          DOWNLOAD_PROGRESS to progress,
          DOWNLOAD_STATE to state.ordinal
        )

      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.UNMETERED)
        .setRequiresStorageNotLow(true)
        .build()

      val startDownloadWorkRequest = OneTimeWorkRequestBuilder<UpdateDownloadWorker>()
        .setInputData(downloadData)
        .addTag(DOWNLOAD_WORKER_TAG)
        .build()

      val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
        .setConstraints(constraints)
        .addTag(DOWNLOAD_WORKER_TAG)
        .build()

      workManager
        .beginUniqueWork(
          downloadId,
          ExistingWorkPolicy.REPLACE,
          startDownloadWorkRequest
        )
        .then(downloadWorkRequest)
        .enqueue()
    }
  }
}
