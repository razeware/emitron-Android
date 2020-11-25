package com.razeware.emitron.ui.player.workers

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.*
import com.razeware.emitron.data.progressions.ProgressionRepository
import com.razeware.emitron.model.Contents
import retrofit2.HttpException
import java.io.IOException

/**
 *  Worker for updating offline progression
 *
 */
class UpdateOfflineProgressWorker @WorkerInject constructor(
  @Assisted appContext: Context,
  @Assisted workerParameters: WorkerParameters,
  /**
   * Progression repository
   */
  val progressionRepository: ProgressionRepository
) : CoroutineWorker(appContext, workerParameters) {

  /**
   * See [Worker.doWork]
   */
  override suspend fun doWork(): Result {
    updateProgress()
    updateWatchStats()
    return Result.success()
  }

  private suspend fun updateProgress() {
    val progressions = progressionRepository.getLocalProgressions()

    if (progressions.isEmpty() || progressions.all { it.synced }) {
      return
    }

    val contents = Contents.from(*(progressions.map {
      it.toData()
    }.toTypedArray()))

    val response =
      try {
        progressionRepository.updateProgressions(contents)
      } catch (exception: IOException) {
        null
      } catch (exception: HttpException) {
        null
      }

    response ?: Result.failure()
    val responseBody = response?.datum
    responseBody ?: Result.failure()

    if (responseBody.isNullOrEmpty()) {

      // Mark local progressions synced
      progressionRepository.updateLocalProgressions(progressions.map {
        it.copy(synced = true)
      })
    } else {

      // Update local with server response
      progressionRepository.updateLocalProgressions(
        responseBody.map {
          it.toProgression()
        }
      )
    }
  }

  private suspend fun updateWatchStats() {
    val watchStats = progressionRepository.getWatchStats()
    val contents = Contents.from(*(watchStats.map {
      it.toData()
    }.toTypedArray()))
    val response = progressionRepository.updateWatchStats(contents)
    if (response.isSuccessful) {
      progressionRepository.deleteWatchStats()
    }
  }

  companion object {

    private const val PROGRESS_WORKER_TAG: String = "offline_progress"

    private const val PROGRESS_WORKER_NAME: String = "sync_offline_progress"

    /**
     * Queue verify download worker
     *
     * @param workManager WorkManager
     */
    fun enqueue(workManager: WorkManager) {

      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

      val updateOfflineProgressRequest =
        OneTimeWorkRequestBuilder<UpdateOfflineProgressWorker>()
          .setConstraints(constraints)
          .addTag(PROGRESS_WORKER_TAG)
          .build()

      workManager
        .enqueueUniqueWork(
          PROGRESS_WORKER_NAME,
          ExistingWorkPolicy.REPLACE,
          updateOfflineProgressRequest
        )
    }
  }
}
