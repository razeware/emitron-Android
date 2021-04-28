package com.razeware.emitron.ui.download

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
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.util.NotificationUtil
import com.google.android.exoplayer2.util.Util
import com.razeware.emitron.R
import com.razeware.emitron.data.settings.SettingsRepository
import com.razeware.emitron.model.DownloadState
import com.razeware.emitron.notifications.NotificationChannels
import com.razeware.emitron.ui.download.workers.UpdateDownloadWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Aliasing for name conflict
 */
typealias ExoDownloadService = com.google.android.exoplayer2.offline.DownloadService

/**
 * Content Download Service
 */
@AndroidEntryPoint
class DownloadService : ExoDownloadService(
  FOREGROUND_NOTIFICATION_ID,
  DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
  NotificationChannels.channelIdDownloads,
  R.string.notification_channel_downloads,
  R.string.notification_channel_downloads_description
) {

  private var nextNotificationId = FOREGROUND_NOTIFICATION_ID + 1

  private val notificationHelper: DownloadNotificationHelper by lazy {
    DownloadNotificationHelper(this, NotificationChannels.channelIdDownloads)
  }

  /**
   * Download Manager
   */
  @Inject
  lateinit var eDownloadManager: DownloadManager

  /**
   * Settings Repository
   */
  @Inject
  lateinit var settingsRepository: SettingsRepository

  init {
    nextNotificationId = FOREGROUND_NOTIFICATION_ID + 1
  }

  /**
   * See [DownloadService.getDataDir]
   */
  override fun getDownloadManager(): DownloadManager {
    return eDownloadManager
  }

  override fun onCreate() {
    super.onCreate()
    eDownloadManager.addListener(object : DownloadManager.Listener {
      override fun onDownloadChanged(
        downloadManager: DownloadManager, download: Download, finalException: Exception?
      ) {
        super.onDownloadChanged(downloadManager, download, finalException)
        changeDownloadNotification(download)
      }
    })
  }

  /**
   * See [DownloadService.getForegroundNotification]
   */
  override fun getForegroundNotification(downloads: MutableList<Download>): Notification {
    val message = if (!downloads.isNullOrEmpty()) {
      downloads.firstOrNull {
        it.state == Download.STATE_DOWNLOADING
      }?.request?.data?.toString(Charsets.UTF_8)
    } else {
      null
    }
    return notificationHelper.buildProgressNotification(
      this,
      R.drawable.ic_logo,
      null,
      message,
      downloads
    )
  }

  /**
   * See [DownloadService.getScheduler]
   */
  override fun getScheduler(): Scheduler {
    return PlatformScheduler(this, JOB_ID)
  }

  internal fun changeDownloadNotification(download: Download) {
    val notification: Notification? = when (download.state) {
      Download.STATE_COMPLETED -> {
        download.run {
          handleDownloadCompleted(download)
          buildDownloadCompletedNotification(download)
        }
      }
      Download.STATE_FAILED -> {
        handleDownloadFailed(download)
        buildDownloadFailedNotification(download)
      }
      Download.STATE_REMOVING -> {
        handleDownloadRemoved(download)
        null
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
      state = DownloadState.COMPLETED,
      downloadOnlyOnWifi = settingsRepository.getDownloadsWifiOnly()
    )
  }

  private fun handleDownloadFailed(download: Download) {
    UpdateDownloadWorker.updateAndStartNext(
      WorkManager.getInstance(this),
      downloadId = download.request.id,
      progress = download.percentDownloaded.roundToInt(),
      state = DownloadState.FAILED,
      downloadOnlyOnWifi = settingsRepository.getDownloadsWifiOnly()
    )
  }

  private fun handleDownloadRemoved(download: Download) {
    UpdateDownloadWorker.updateAndStartNext(
      WorkManager.getInstance(this),
      downloadId = download.request.id,
      progress = 0,
      state = DownloadState.NONE,
      downloadOnlyOnWifi = settingsRepository.getDownloadsWifiOnly()
    )
  }

  private fun buildDownloadCompletedNotification(download: Download): Notification {
    return download.run {
      notificationHelper.buildDownloadCompletedNotification(
        this@DownloadService,
        R.drawable.ic_file_download,
        null,
        Util.fromUtf8Bytes(download.request.data)
      )
    }
  }

  private fun buildDownloadFailedNotification(download: Download): Notification {
    return download.run {
      notificationHelper.buildDownloadFailedNotification(
        this@DownloadService,
        R.drawable.ic_file_download,
        null,
        Util.fromUtf8Bytes(download.request.data)
      )
    }
  }

  companion object {
    private const val JOB_ID = 1
    private const val FOREGROUND_NOTIFICATION_ID = 1

    /**
     * Build [CacheDataSourceFactory]
     */
    fun buildCacheDataSourceFactory(cache: Cache, userAgent: String): CacheDataSource.Factory =
      CacheDataSource.Factory().apply {
        setCache(cache)
        setUpstreamDataSourceFactory(buildHttpDataSourceFactory(userAgent))
      }

    /**
     * Build [HttpDataSource.Factory]
     */
    fun buildHttpDataSourceFactory(userAgent: String): HttpDataSource.Factory {
      return DefaultHttpDataSource.Factory().apply { setUserAgent(userAgent) }
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
      uri: Uri,
      name: String?
    ): String {
      val downloadRequest = DownloadRequest.Builder(contentId, uri)
        .setData(name?.toByteArray())
        .build()

      sendAddDownload(
        ctx, DownloadService::class.java, downloadRequest, true
      )
      return downloadRequest.id
    }

    /**
     * Resume a download
     *
     * @param ctx Context
     * @param contentId Content Id
     *
     */
    fun resumeDownload(
      ctx: Context,
      contentId: String
    ) {
      sendSetStopReason(
        ctx, DownloadService::class.java,
        contentId,
        Download.STOP_REASON_NONE,
        true
      )
    }

    /**
     * Pause a download
     *
     * @param ctx Context
     * @param contentId Content Id
     *
     */
    fun pauseDownload(
      ctx: Context,
      contentId: String
    ) {
      sendSetStopReason(
        ctx, DownloadService::class.java,
        contentId,
        1,
        true
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
        ctx, DownloadService::class.java, contentId, true
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
        ctx, DownloadService::class.java, true
      )
    }
  }
}
