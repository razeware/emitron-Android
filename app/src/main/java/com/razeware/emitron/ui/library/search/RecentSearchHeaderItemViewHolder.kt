package com.razeware.emitron.ui.library.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.razeware.emitron.databinding.ItemRecentSearchHeaderBinding

/**
 * View holder for filter options group header
 */
class RecentSearchHeaderItemViewHolder(binding: ItemRecentSearchHeaderBinding) :
  RecyclerView.ViewHolder(binding.root) {

  companion object {

    /**
     * Factory function to create [RecentSearchHeaderItemViewHolder]
     */
    fun create(parent: ViewGroup, layoutId: Int): RecentSearchHeaderItemViewHolder =
      RecentSearchHeaderItemViewHolder(
        DataBindingUtil.inflate(
          LayoutInflater.from(parent.context),
          layoutId, parent, false
        )
      )
  }
}
