package com.raywenderlich.emitron.ui.settings

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.raywenderlich.emitron.data.settings.SettingsRepository
import com.raywenderlich.emitron.di.modules.worker.ChildWorkerFactory
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

/**
 * Worker for stopping downloads
 */
class SignOutWorker @AssistedInject constructor(
  @Assisted private val appContext: Context,
  @Assisted private val workerParameters: WorkerParameters,
  private val settingsRepository: SettingsRepository
) : Worker(appContext, workerParameters) {

  /**
   * See [Worker.doWork]
   */
  override fun doWork(): Result {
    // Clear tables and preferences
    settingsRepository.logout()
    return Result.success()
  }

  /**
   * Factory for [SignOutWorker]
   */
  @AssistedInject.Factory
  interface Factory : ChildWorkerFactory

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
