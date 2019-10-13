package com.raywenderlich.emitron.ui.download

import android.app.Notification
import android.content.Context
import android.net.Uri
import androidx.work.WorkManager
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadRequest
import com.google.android.exoplayer2.scheduler.PlatformScheduler
import com.google.android.exoplayer2.scheduler.Scheduler
import com.google.android.exoplayer2.ui.DownloadNotificationHelper
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.util.NotificationUtil
import com.google.android.exoplayer2.util.Util
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.model.DownloadState
import com.raywenderlich.emitron.notifications.NotificationChannels
import com.raywenderlich.emitron.ui.download.workers.UpdateDownloadWorker
import dagger.android.AndroidInjection
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Aliasing for name conflict
 */
typealias ExoDownloadService = com.google.android.exoplayer2.offline.DownloadService

/**
 * Content Download Service
 */
class DownloadService : ExoDownloadService(
  FOREGROUND_NOTIFICATION_ID,
  DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
  NotificationChannels.channelIdDownloads,
  R.string.notification_channel_downloads,
  R.string.notification_channel_downloads_description
) {

  private var nextNotificationId = FOREGROUND_NOTIFICATION_ID + 1

  private var notificationHelper: DownloadNotificationHelper? = null

  @Inject
  lateinit var eDownloadManager: DownloadManager

  init {
    nextNotificationId = FOREGROUND_NOTIFICATION_ID + 1
  }

  /**
   * See [DownloadService.onCreate]
   */
  override fun onCreate() {
    AndroidInjection.inject(this)
    super.onCreate()
    notificationHelper = DownloadNotificationHelper(
      this,
      NotificationChannels.channelIdDownloads
    )
  }

  /**
   * See [DownloadService.getDataDir]
   */
  override fun getDownloadManager(): DownloadManager {
    return eDownloadManager
  }

  /**
   * See [DownloadService.getForegroundNotification]
   */
  override fun getForegroundNotification(downloads: MutableList<Download>?): Notification {
    return notificationHelper?.buildProgressNotification(
      R.drawable.ic_logo,
      null,
      null,
      downloads
    )!!
  }

  /**
   * See [DownloadService.getScheduler]
   */
  override fun getScheduler(): Scheduler? {
    return PlatformScheduler(this, JOB_ID)
  }

  /**
   * See [DownloadService.onDownloadChanged]
   */
  override fun onDownloadChanged(download: Download?) {
    super.onDownloadChanged(download)
    val notification: Notification? = when {
      download?.state == Download.STATE_COMPLETED -> {
        download.run {
          handleDownloadCompleted(download)
          buildDownloadCompletedNotification(download)
        }
      }
      download?.state == Download.STATE_FAILED -> {
        handleDownloadFailed(download)
        buildDownloadFailedNotification(download)
      }
      else -> return
    }
    NotificationUtil.setNotification(this, nextNotificationId++, notification)
  }

  private fun handleDownloadCompleted(download: Download) {
    UpdateDownloadWorker.updateAndStartNext(
      WorkManager.getInstance(this),
      downloadId = download.request.id,
      progress = download.percentDownloaded.roundToInt(),
      state = DownloadState.COMPLETED
    )
  }

  private fun handleDownloadFailed(download: Download) {
    UpdateDownloadWorker.updateAndStartNext(
      WorkManager.getInstance(this),
      downloadId = download.request.id,
      progress = download.percentDownloaded.roundToInt(),
      state = DownloadState.FAILED
    )
  }

  private fun buildDownloadCompletedNotification(download: Download): Notification? {
    return download.run {
      notificationHelper?.buildDownloadCompletedNotification(
        R.drawable.ic_file_download,
        null,
        Util.fromUtf8Bytes(download.request?.data)
      )
    }
  }

  private fun buildDownloadFailedNotification(download: Download): Notification? {
    return download.run {
      notificationHelper?.buildDownloadFailedNotification(
        R.drawable.ic_file_download,
        null,
        Util.fromUtf8Bytes(download.request?.data)
      )
    }
  }


  companion object {
    private const val JOB_ID = 1
    private const val FOREGROUND_NOTIFICATION_ID = 1

    /**
     * Build [CacheDataSourceFactory]
     */
    fun buildCacheDataSourceFactory(cache: Cache, userAgent: String): CacheDataSourceFactory =
      CacheDataSourceFactory(cache, buildHttpDataSourceFactory(userAgent))

    /**
     * Build [HttpDataSource.Factory]
     */
    fun buildHttpDataSourceFactory(userAgent: String): HttpDataSource.Factory {
      return DefaultHttpDataSourceFactory(userAgent)
    }

    /**
     * Start a download
     *
     * @param ctx Context
     * @param contentId Content Id
     * @param uri Download uri
     *
     * @return Download id (Content id)
     */
    fun startDownload(
      ctx: Context,
      contentId: String,
      uri: Uri
    ): String {
      val downloadRequest = DownloadRequest(
        contentId,
        DownloadRequest.TYPE_PROGRESSIVE,
        uri,
        Collections.emptyList(),
        null,
        null
      )
      sendAddDownload(
        ctx, DownloadService::class.java, downloadRequest, true
      )
      return downloadRequest.id
    }

    fun resumeDownload(
      ctx: Context,
      contentId: String
    ) {
      sendSetStopReason(
        ctx, DownloadService::class.java,
        contentId,
        Download.STOP_REASON_NONE,
        false
      )
    }

    fun pauseDownload(
      ctx: Context,
      contentId: String
    ) {
      sendSetStopReason(
        ctx, DownloadService::class.java,
        contentId,
        1,
        false
      )
    }

    /**
     * Stop a download
     *
     * @param ctx Context
     * @param contentId Content Id
     *
     */
    fun removeDownload(
      ctx: Context,
      contentId: String
    ) {
      sendRemoveDownload(
        ctx, DownloadService::class.java, contentId, false
      )
    }

    /**
     * Remove all downloads
     *
     * @param ctx Context
     */
    fun removeAllDownloads(
      ctx: Context
    ) {
      sendRemoveAllDownloads(
        ctx, DownloadService::class.java, false
      )
    }
  }
}