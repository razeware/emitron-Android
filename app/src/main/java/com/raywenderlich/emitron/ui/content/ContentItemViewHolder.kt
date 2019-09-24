package com.raywenderlich.emitron.ui.content

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.emitron.databinding.ItemContentBinding
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.utils.extensions.toVisibility

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
    binding.root.setOnClickListener {
      onItemClick(adapterPosition)
    }
    binding.data = content
    binding.releaseDateWithTypeAndDuration =
      content?.getReadableReleaseAtWithTypeAndDuration(binding.root.context)
    binding.textLanguage.text = content?.getDomain()

    binding.progressContentProgression.toVisibility(
      !adapterContent.isContent()
          && !adapterContent.isBookmarked()
          && !adapterContent.isCompleted()
    )
    binding.textCollectionLabelPro.toVisibility(
      adapterContent.isContent() &&
          content?.isFinished() != true &&
          content?.isFreeContent() != true
    )
    binding.buttonBookmark.toVisibility(adapterContent.isBookmarked())
    binding.buttonBookmark.setOnClickListener {
      bookmarkCallback?.invoke(adapterPosition)
    }
    binding.buttonDownload.setOnClickListener {
      downloadCallback?.invoke(adapterPosition, 1)
    }
    binding.buttonDownloadStop.setOnClickListener {
      downloadCallback?.invoke(adapterPosition, 0)
    }
    binding.executePendingBindings()
  }

  /**
   * Clear up
   * Remove click listener
   */
  fun unBind() {
    binding.root.setOnClickListener(null)
    binding.buttonBookmark.setOnClickListener(null)
    binding.buttonDownload.setOnClickListener(null)
    binding.buttonDownloadStop.setOnClickListener(null)
  }

  companion object {

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
