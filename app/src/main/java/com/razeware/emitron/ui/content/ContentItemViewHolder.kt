package com.razeware.emitron.ui.content

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.razeware.emitron.databinding.ItemContentBinding
import com.razeware.emitron.model.Data
import com.razeware.emitron.utils.extensions.toVisibility

/**
 * View holder to represent content item in library, bookmark, downloads and progression UI
 */
class ContentItemViewHolder(private val binding: ItemContentBinding) :
  RecyclerView.ViewHolder(binding.root) {

  /**
   * @param content [Data] for this item layout
   * @param onItemClick Click listener for this item layout
   */
  fun bindTo(
    content: Data?,
    adapterContent: ContentAdapter.AdapterContentType,
    onItemClick: (Int) -> Unit,
    bookmarkCallback: ((Int) -> Unit)? = null,
    downloadCallback: ((Int, Int) -> Unit)? = null
  ) {

    with(binding) {
      root.setOnClickListener {
        onItemClick(adapterPosition)
      }
      data = content
      releaseDateWithTypeAndDuration =
        content?.getReadableReleaseAtWithTypeAndDuration(root.context)
      textLanguage.text = content?.getReadableDomain(root.context)

      setProgress(content, adapterContent)
      textCollectionLabelPro.toVisibility(
        content?.isProgressionFinished() != true &&
            content?.isProfessional() == true
      )
      // Hide the download button if the content is downloaded and we are on downloads list
      val showDownloadButton =
        content?.isNotDownloaded() == true && adapterContent.isDownloaded()
      buttonDownload.toVisibility(showDownloadButton)
      buttonDownload.updateDownloadState(content?.download)

      buttonDownload.setOnClickListener {
        downloadCallback?.invoke(adapterPosition, 1)
      }

      buttonBookmark.toVisibility(
        adapterContent.isContent() ||
            adapterContent.isBookmarked()
      )
      buttonBookmark.setOnClickListener {
        if (adapterContent.isBookmarked()) {
          bookmarkCallback?.invoke(adapterPosition)
        }
      }
      executePendingBindings()
    }
  }

  private fun setProgress(content: Data?, adapterContent: ContentAdapter.AdapterContentType) {
    with(binding) {
      val progress = content?.getProgressionPercentComplete() ?: 0

      val contentIsInProgress = null != content?.getProgressionId() && progress < DEFAULT_PROGRESS
      val updatedProgress =
        if (contentIsInProgress) {
          DEFAULT_PROGRESS
        } else {
          progress
        }

      progressContentProgression.toVisibility(
        content?.isProgressionFinished() != true &&
            !adapterContent.isCompleted()
      )
      progressContentProgression.progress = updatedProgress
    }
  }

  /**
   * Clear up
   * Remove click listener
   */
  fun unBind() {
    with(binding) {
      root.setOnClickListener(null)
      buttonBookmark.setOnClickListener(null)
      buttonDownload.setOnClickListener(null)
    }
  }

  companion object {

    /**
     * In progress items will show a minimum progress if the items are in progress and
     * the progress percentage is 0
     */
    const val DEFAULT_PROGRESS: Int = 10

    /**
     * Factory function to create [ContentItemViewHolder]
     */
    fun create(parent: ViewGroup, layoutId: Int): ContentItemViewHolder =
      ContentItemViewHolder(
        DataBindingUtil.inflate(
          LayoutInflater.from(parent.context),
          layoutId, parent, false
        )
      )
  }
}
