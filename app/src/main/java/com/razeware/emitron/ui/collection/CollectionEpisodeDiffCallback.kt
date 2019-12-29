package com.razeware.emitron.ui.collection

import androidx.recyclerview.widget.DiffUtil
import com.razeware.emitron.model.DownloadState

/**
 * Use [DiffUtil] to figure out changing state in collection episode list
 */
class CollectionEpisodeDiffCallback : DiffUtil.ItemCallback<CollectionEpisode>() {

  /**
   * See [DiffUtil.ItemCallback.areItemsTheSame]
   */
  override fun areItemsTheSame(old: CollectionEpisode, new: CollectionEpisode): Boolean {
    return if (old.hasTitle() && new.hasTitle()) {
      old.title == new.title
    } else {
      old.data?.id == new.data?.id
    }
  }

  /**
   * See [DiffUtil.ItemCallback.areContentsTheSame]
   */
  override fun areContentsTheSame(old: CollectionEpisode, new: CollectionEpisode): Boolean {
    return if (old.hasTitle() && new.hasTitle()) {
      old.title == new.title
    } else {
      val wasCompleted = old.data?.isProgressionFinished() ?: false
      val isCompleted = new.data?.isProgressionFinished() ?: false

      val wasDownloaded = old.data?.isDownloaded() ?: false
      val isDownloaded = new.data?.isDownloaded() ?: false

      val oldDownloadProgress = old.data?.getDownloadProgress() ?: 0
      val newDownloadProgress = new.data?.getDownloadProgress() ?: 0

      val oldDownloadState = old.data?.getDownloadState() ?: DownloadState.PAUSED.ordinal
      val newDownloadState = new.data?.getDownloadState() ?: DownloadState.PAUSED.ordinal

      wasCompleted == isCompleted &&
          wasDownloaded == isDownloaded &&
          oldDownloadProgress == newDownloadProgress &&
          oldDownloadState == newDownloadState
    }
  }
}
