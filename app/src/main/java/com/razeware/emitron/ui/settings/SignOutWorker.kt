package com.razeware.emitron.ui.settings

import android.content.Context
import androidx.work.*
import com.razeware.emitron.data.settings.SettingsRepository
import com.razeware.emitron.ui.download.DownloadService
import com.razeware.emitron.utils.extensions.injectWorker
import javax.inject.Inject

/**
 * Worker for stopping downloads
 */
class SignOutWorker(
  private val appContext: Context,
  workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {

  /**
   * Settings repository
   */
  @Inject
  lateinit var settingsRepository: SettingsRepository

  init {
    appContext.injectWorker(this)
  }

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
