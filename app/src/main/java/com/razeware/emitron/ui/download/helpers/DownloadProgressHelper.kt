package com.razeware.emitron.ui.download.helpers

import android.content.Context
import android.os.Handler
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.razeware.emitron.model.DownloadProgress
import com.razeware.emitron.model.DownloadState
import com.razeware.emitron.utils.createMainThreadScheduledHandler
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Helper class to observe progress of downloads from [DownloadManager]
 */
class DownloadProgressHelper @Inject constructor(private val downloadManager: DownloadManager) {

  /**
   * Download listener
   */
  class DownloadStartListener(private val onDownloadStart: () -> Unit) : DownloadManager.Listener {

    /**
     * See [DownloadManager.Listener.onDownloadChanged]
     */
    override fun onDownloadChanged(downloadManager: DownloadManager?, download: Download?) {
      if (download?.state == Download.STATE_DOWNLOADING
        || download?.state == Download.STATE_RESTARTING
      ) {
        onDownloadStart()
      }
    }
  }

  private var downloadProgressHandler: Handler? = null

  private var downloadStartListener: DownloadStartListener? = null

  /**
   * Start observing download manager changes
   */
  fun init(
    isFragmentVisible: Boolean,
    context: Context,
    contentIds: List<String>,
    onDownloadChange: (DownloadProgress) -> Unit
  ) {
    val downloads = downloadManager.currentDownloads

    if (!downloads.isNullOrEmpty()) {
      val downloadIds = downloads.map {
        it.request.id
      }.intersect(contentIds)

      if (downloadIds.isNotEmpty()) {
        createDownloadProgressHandler(
          isFragmentVisible,
          context,
          contentIds,
          onDownloadChange
        )
      }
    }
    if (null == downloadStartListener) {
      downloadStartListener =
        DownloadStartListener {
          createDownloadProgressHandler(
            isFragmentVisible,
            context,
            contentIds,
            onDownloadChange
          )
        }
      downloadManager.addListener(downloadStartListener)
    }
  }

  private fun updateDownloadProgress(
    contentIds: List<String>,
    onDownloadChange: (DownloadProgress) -> Unit
  ) {
    val downloads = downloadManager.currentDownloads

    val downloadIds = downloads.map {
      it.request.id
    }.intersect(contentIds)

    if (downloadIds.isEmpty()) {
      downloadProgressHandler?.removeCallbacksAndMessages(null)
      return
    }

    downloads.map {
      val state = when (it.state) {
        Download.STATE_FAILED -> DownloadState.FAILED
        Download.STATE_COMPLETED -> DownloadState.COMPLETED
        Download.STATE_DOWNLOADING -> DownloadState.IN_PROGRESS
        Download.STATE_QUEUED -> DownloadState.PAUSED
        Download.STATE_STOPPED -> DownloadState.PAUSED
        else -> DownloadState.IN_PROGRESS
      }
      onDownloadChange(
        DownloadProgress(
          it.request.id,
          it.percentDownloaded.roundToInt(),
          state
        )
      )
    }
  }

  private fun createDownloadProgressHandler(
    isFragmentVisible: Boolean,
    context: Context,
    contentIds: List<String>,
    onDownloadChange: (DownloadProgress) -> Unit
  ) {
    if (!isFragmentVisible) {
      return
    }

    if (null != downloadProgressHandler) {
      return
    }
    downloadProgressHandler =
      createMainThreadScheduledHandler(
        context,
        DOWNLOAD_PROGRESS_UPDATE_INTERVAL_MILLIS
      ) {
        updateDownloadProgress(contentIds, onDownloadChange)
      }
  }

  /**
   * Clear download progress helper
   */
  fun clear() {
    downloadStartListener = null
    downloadProgressHandler?.removeCallbacksAndMessages(null)
  }

  companion object {
    /**
     * Handler interval to update download progress
     */
    const val DOWNLOAD_PROGRESS_UPDATE_INTERVAL_MILLIS: Long = 5000L
  }
}
