package com.razeware.emitron.ui.settings

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.*
import com.razeware.emitron.data.settings.SettingsRepository
import com.razeware.emitron.ui.download.DownloadService

/**
 * Worker for stopping downloads
 */
class SignOutWorker @WorkerInject constructor(
  /** Context */
  @Assisted val appContext: Context,
  @Assisted workerParameters: WorkerParameters,
  /** Settings Repository */
  val settingsRepository: SettingsRepository
) : CoroutineWorker(appContext, workerParameters) {

  /**
   * See [Worker.doWork]
   */
  override suspend fun doWork(): Result {
    // Clear tables and preferences
    settingsRepository.logout()
    DownloadService.removeAllDownloads(appContext)
    return Result.success()
  }

  companion object {

    private const val DOWNLOAD_WORKER_TAG: String = "downloads"

    /**
     * Stop content download
     *
     * @param workManager WorkManager
     */
    fun enqueue(workManager: WorkManager) {

      val workRequest = OneTimeWorkRequestBuilder<SignOutWorker>()
        .addTag(DOWNLOAD_WORKER_TAG)
        .build()

      workManager.enqueue(workRequest)
    }
  }
}
