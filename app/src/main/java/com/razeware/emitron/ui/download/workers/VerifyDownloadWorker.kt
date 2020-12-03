package com.razeware.emitron.ui.download.workers

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.*
import com.razeware.emitron.data.login.LoginRepository
import com.razeware.emitron.model.PermissionTag
import com.razeware.emitron.model.isDownloadPermissionTag
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 *  Worker for verifying a downloads every 7th day,
 *
 * It will fetch the permissions, if it fails due to any issue,
 * existing download permissions will be removed.
 */
class VerifyDownloadWorker @WorkerInject constructor(
  @Assisted appContext: Context,
  @Assisted workerParameters: WorkerParameters,
  /**
   * Login Repository
   * */
  val loginRepository: LoginRepository
) : CoroutineWorker(appContext, workerParameters) {

  /**
   * See [Worker.doWork]
   */
  override suspend fun doWork(): Result {
    try {
      val response = loginRepository.getPermissions()
      val permissions = response.datum.mapNotNull {
        it.getTag()
      }
      if (permissions.isNotEmpty()) {
        val userPermissions =
          PermissionTag.values().map { it.param }.toSet().intersect(permissions).toList()
        loginRepository.updatePermissions(userPermissions)
      } else {
        loginRepository.removePermissions()
      }
    } catch (exception: HttpException) {
      removeDownloadPermission()
    } catch (exception: IOException) {
      removeDownloadPermission()
    }
    return Result.success()
  }

  private fun removeDownloadPermission() {
    loginRepository.updatePermissions(
      loginRepository.getPermissionsFromPrefs().filter {
        PermissionTag.fromParam(it).isDownloadPermissionTag()
      }
    )
  }

  companion object {

    private const val DOWNLOAD_WORKER_TAG: String = "downloads"

    private const val VERIFY_DOWNLOAD_INTERVAL = 7L

    /**
     * Queue verify download worker
     *
     * @param workManager WorkManager
     */
    fun queue(workManager: WorkManager) {
      val verifyDownloadWorkRequest =
        PeriodicWorkRequestBuilder<VerifyDownloadWorker>(
          VERIFY_DOWNLOAD_INTERVAL,
          TimeUnit.DAYS
        ).addTag(DOWNLOAD_WORKER_TAG)
          .build()
      workManager.enqueue(verifyDownloadWorkRequest)
    }
  }
}
