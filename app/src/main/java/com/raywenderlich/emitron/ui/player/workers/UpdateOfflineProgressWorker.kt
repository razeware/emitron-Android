package com.raywenderlich.emitron.ui.player.workers

import android.content.Context
import androidx.work.*
import com.raywenderlich.emitron.data.progressions.ProgressionRepository
import com.raywenderlich.emitron.di.modules.worker.ChildWorkerFactory
import com.raywenderlich.emitron.model.Contents
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import retrofit2.HttpException
import java.io.IOException

/**
 *  Worker for updating offline progression
 *
 */
class UpdateOfflineProgressWorker @AssistedInject constructor(
  @Assisted private val appContext: Context,
  @Assisted private val workerParameters: WorkerParameters,
  private val progressionRepository: ProgressionRepository
) : CoroutineWorker(appContext, workerParameters) {

  /**
   * See [Worker.doWork]
   */
  override suspend fun doWork(): Result {
    val progressions = progressionRepository.getLocalProgressions()

    if (progressions.isEmpty() || progressions.all { it.synced }) {
      return Result.success()
    }

    val contents = Contents.from(*(progressions.map {
      it.toData()
    }.toTypedArray()))

    val response =
      try {
        progressionRepository.updateProgression(contents)
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
    return Result.success()
  }

  /**
   * [UpdateOfflineProgressWorker.Factory]
   */
  @AssistedInject.Factory
  interface Factory : ChildWorkerFactory

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
