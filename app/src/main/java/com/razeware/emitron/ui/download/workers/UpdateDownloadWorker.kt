package com.razeware.emitron.ui.download.workers

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.*
import com.razeware.emitron.data.download.DownloadRepository
import com.razeware.emitron.model.DownloadProgress
import com.razeware.emitron.model.DownloadState
import com.razeware.emitron.ui.download.DownloadService

/**
 *  Worker for updating a download,
 *
 * It will fetch content to be downloaded and add it to db.
 * It will be followed by [DownloadWorker] which will read from database and forward downloads
 * to [DownloadService]
 */
class UpdateDownloadWorker @WorkerInject constructor(
  @Assisted appContext: Context,
  @Assisted workerParameters: WorkerParameters,
  /**
   * Download repository.
   * */
  val downloadRepository: DownloadRepository
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
      state: DownloadState,
      downloadOnlyOnWifi: Boolean
    ) {
      val downloadData =
        workDataOf(
          DOWNLOAD_ID to downloadId,
          DOWNLOAD_PROGRESS to progress,
          DOWNLOAD_STATE to state.ordinal
        )

      val startDownloadWorkRequest = OneTimeWorkRequestBuilder<UpdateDownloadWorker>()
        .setInputData(downloadData)
        .build()

      val downloadWorkRequest =
        DownloadWorker.buildWorkRequest(downloadOnlyOnWifi)

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
