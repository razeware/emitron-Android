package com.razeware.emitron.ui.download.helpers

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.work.WorkManager
import com.razeware.emitron.R
import com.razeware.emitron.ui.download.workers.RemoveDownloadWorker
import com.razeware.emitron.ui.download.workers.StartDownloadWorker
import com.razeware.emitron.utils.extensions.createDialog
import com.razeware.emitron.utils.extensions.isNetNotConnected
import com.razeware.emitron.utils.extensions.showErrorSnackbar

/**
 * Helper class to start/stop download
 */
class DownloadHelper(private val fragment: Fragment) {

  private var removeDownloadDialog: AlertDialog? = null

  /**
   * Start Download
   */
  fun startDownload(
    isDownloadAllowed: Boolean = false,
    contentId: String?,
    contentIsDownloaded: Boolean = false,
    episodeId: String? = null,
    episodeIsDownloaded: Boolean = false,
    onDownloadStarted: (() -> Unit)? = null,
    onDownloadRemoved: ((String) -> Unit)? = null,
    downloadsWifiOnly: Boolean
  ) {

    contentId ?: return

    // Delete downloaded episode
    if (!episodeId.isNullOrBlank() && episodeIsDownloaded) {
      showDeleteDownloadedContentDialog(episodeId, onDownloadRemoved)
      return
    }

    // Delete downloaded collection
    if (contentIsDownloaded) {
      showDeleteDownloadedContentDialog(contentId, onDownloadRemoved)
      return
    }

    if (!isDownloadAllowed) {
      fragment.showErrorSnackbar(fragment.getString(R.string.message_downloads_no_subscription))
      return
    }

    if (fragment.isNetNotConnected()) {
      fragment.showErrorSnackbar(fragment.getString(R.string.error_no_connection))
      return
    }

    StartDownloadWorker.enqueue(
      WorkManager.getInstance(fragment.requireContext()),
      contentId,
      episodeId,
      downloadsWifiOnly
    )

    onDownloadStarted?.invoke()
  }

  /**
   * Show Dialog to delete content
   *
   * @param downloadId Download Id
   * @param onDownloadRemoved On download r
   */
  fun showDeleteDownloadedContentDialog(
    downloadId: String,
    onDownloadRemoved: ((String) -> Unit)? = null
  ) {
    if (isShowingRemoveDownloadDialog()) {
      return
    }
    removeDownloadDialog = fragment.createDialog(
      title = R.string.title_download_remove,
      message = R.string.message_download_remove,
      positiveButton = R.string.button_label_yes,
      positiveButtonClickListener = {
        handleRemoveDownload(downloadId, onDownloadRemoved)
      },
      negativeButton = R.string.button_label_no
    )
    removeDownloadDialog?.show()
  }

  private fun handleRemoveDownload(
    downloadId: String,
    onDownloadRemoved: ((String) -> Unit)? = null
  ) {
    onDownloadRemoved?.invoke(downloadId)
    RemoveDownloadWorker.enqueue(
      WorkManager.getInstance(fragment.requireContext()),
      downloadId
    )
  }

  /**
   * Clear pending Dialogs
   */
  fun clear() {
    if (isShowingRemoveDownloadDialog()) {
      removeDownloadDialog?.dismiss()
    }
  }

  private fun isShowingRemoveDownloadDialog() = removeDownloadDialog?.isShowing == true
}
