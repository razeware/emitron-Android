package com.raywenderlich.emitron.ui.content

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.emitron.databinding.ItemLibraryBinding
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.ui.common.ItemErrorViewHolder.Companion.toVisibility

/**
 * View holder to represent content item in library, bookmark, downloads and progression UI
 */
class ContentItemViewHolder(private val binding: ItemLibraryBinding) :
  RecyclerView.ViewHolder(binding.root) {

  /**
   * @param content [Data] for this item layout
   * @param onItemClick Click listener for this item layout
   */
  fun bindTo(
    content: Data?,
    contentAdapterType: ContentAdapter.ContentAdapterType,
    onItemClick: (Int) -> Unit
  ) {
    binding.root.setOnClickListener {
      onItemClick(adapterPosition)
    }
    binding.data = content
    binding.releaseDateWithTypeAndDuration =
      content?.getReadableReleaseAtWithTypeAndDuration(binding.root.context)

    binding.progressCompletion.visibility = toVisibility(!contentAdapterType.isContent())
    binding.buttonBookmark.visibility = toVisibility(!contentAdapterType.isContent())

    binding.executePendingBindings()

  }

  /**
   * Clear up
   * Remove click listener
   */
  fun unBind() {
    binding.root.setOnClickListener(null)
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
