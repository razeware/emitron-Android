package com.razeware.emitron.ui.settings

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.razeware.emitron.data.settings.SettingsRepository
import com.razeware.emitron.ui.download.DownloadService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker for stopping downloads
 */
@HiltWorker
class SignOutWorker @AssistedInject constructor(
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
