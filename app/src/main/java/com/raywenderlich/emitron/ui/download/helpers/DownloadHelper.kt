package com.raywenderlich.emitron.ui.download.helpers

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.work.WorkManager
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.ui.download.workers.RemoveDownloadWorker
import com.raywenderlich.emitron.ui.download.workers.StartDownloadWorker
import com.raywenderlich.emitron.utils.extensions.createDialog
import com.raywenderlich.emitron.utils.extensions.showErrorSnackbar

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
    onDownloadRemoved: ((String) -> Unit)? = null
  ) {

    contentId ?: return

    if (!isDownloadAllowed) {
      fragment.showErrorSnackbar(
        fragment.getString(R.string.message_download_permission_error)
      )
      return
    }

    // Delete downloaded episode
    if (!episodeId.isNullOrBlank() && episodeIsDownloaded) {
      showDeleteDownloadedContentDialog(episodeId)
      return
    }

    // Delete downloaded collection
    if (contentIsDownloaded) {
      showDeleteDownloadedContentDialog(contentId, onDownloadRemoved)
      return
    }

    StartDownloadWorker.enqueue(
      WorkManager.getInstance(fragment.requireContext()),
      contentId,
      episodeId
    )

    onDownloadStarted?.invoke()
  }

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