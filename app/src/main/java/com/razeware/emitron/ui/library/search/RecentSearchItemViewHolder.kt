package com.razeware.emitron.ui.library.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.razeware.emitron.databinding.ItemRecentSearchBinding

/**
 * View holder for filter options group header
 */
class RecentSearchItemViewHolder(private val binding: ItemRecentSearchBinding) :
  RecyclerView.ViewHolder(binding.root) {

  /**
   * Bind recent search item
   *
   * @param title title of recent search
   * @param onItemClick Click listener for this item layout
   */
  fun bindTo(title: String, onItemClick: (Int) -> Unit) {
    binding.root.visibility = View.VISIBLE
    binding.root.setOnClickListener {
      onItemClick(adapterPosition)
    }
    binding.title = title
    binding.executePendingBindings()
  }

  companion object {

    /**
     * Factory function to create [RecentSearchItemViewHolder]
     */
    fun create(parent: ViewGroup, layoutId: Int): RecentSearchItemViewHolder =
      RecentSearchItemViewHolder(
        DataBindingUtil.inflate(
          LayoutInflater.from(parent.context),
          layoutId, parent, false
        )
      )
  }
}
