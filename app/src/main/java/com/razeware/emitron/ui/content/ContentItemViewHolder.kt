package com.razeware.emitron.ui.content

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.razeware.emitron.databinding.ItemContentBinding
import com.razeware.emitron.model.Data

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
    adapterType: ContentAdapter.Type,
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

      handleContentProgression(content, adapterType)
      handleProLabel(content)
      handleDownloadButton(content, adapterType, downloadCallback)
      handleBookmarkButton(adapterType.isBookmarked(), bookmarkCallback)
      executePendingBindings()
    }
  }

  private fun handleProLabel(content: Data?) {
    val showProLabel =
      content?.isProgressionFinished() != true && content?.isProfessional() == true
    binding.textCollectionLabelPro.isVisible = showProLabel
  }

  private fun handleDownloadButton(
    content: Data?,
    adapterType: ContentAdapter.Type,
    downloadCallback: ((Int, Int) -> Unit)? = null
  ) {
    with(binding) {
      // Hide the download button if the content is downloaded and we are on downloads list
      val showDownloadButton =
        content?.isDownloading() == true && adapterType.isDownloaded()
      buttonDownload.isVisible = showDownloadButton
      buttonDownload.updateDownloadState(content?.download)

      buttonDownload.setOnClickListener {
        downloadCallback?.invoke(adapterPosition, 1)
      }
    }
  }

  private fun handleBookmarkButton(
    isContentBookmarked: Boolean,
    bookmarkCallback: ((Int) -> Unit)? = null
  ) {
    with(binding) {
      buttonBookmark.isVisible = isContentBookmarked
      buttonBookmark.setOnClickListener {
        if (isContentBookmarked) {
          bookmarkCallback?.invoke(adapterPosition)
        }
      }
    }
  }

  private fun handleContentProgression(content: Data?, adapterContent: ContentAdapter.Type) {
    with(binding) {
      val progress = content?.getProgressionPercentComplete() ?: 0

      val contentIsInProgress = null != content?.getProgressionId() &&
          progress < DEFAULT_PROGRESS && !content.isProgressionFinished()
      val updatedProgress =
        if (contentIsInProgress) {
          DEFAULT_PROGRESS
        } else {
          progress
        }

      progressContentProgression.isVisible =
        content?.isProgressionFinished() != true && !adapterContent.isCompleted()
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
