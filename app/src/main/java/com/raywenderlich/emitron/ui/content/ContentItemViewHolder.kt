package com.raywenderlich.emitron.ui.content

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.emitron.databinding.ItemLibraryBinding
import com.raywenderlich.emitron.model.Data

/**
 * View holder to represent content item in library, bookmark, downloads and progression UI
 */
class ContentItemViewHolder(private val binding: ItemLibraryBinding) :
  RecyclerView.ViewHolder(binding.root) {

  /**
   * @param data [Data] for this item layout
   * @param onItemClick Click listener for this item layout
   */
  fun bindTo(data: Data?, onItemClick: (Int) -> Unit) {
    binding.root.setOnClickListener {
      onItemClick(adapterPosition)
    }
    binding.data = data
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
