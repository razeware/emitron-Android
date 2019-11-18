package com.razeware.emitron.ui.filter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.razeware.emitron.R
import com.razeware.emitron.databinding.ItemFilterHeaderBinding

/**
 * View holder for filter options group header
 */
class FilterHeaderItemViewHolder(private val binding: ItemFilterHeaderBinding) :
  RecyclerView.ViewHolder(binding.root) {

  /**
   * Bind filter item header
   *
   * @param title title of header
   * @param isSelected true if filter is expanded, else false.
   * @param onItemClick Click listener for this item layout
   */
  fun bindTo(title: String, isSelected: Boolean, onItemClick: (Int) -> Unit) {
    binding.root.visibility = View.VISIBLE
    binding.root.setOnClickListener {
      onItemClick(adapterPosition)
    }
    binding.title = title
    binding.isSelected = isSelected

    if (isSelected) {
      binding.buttonFilterToggle.setIconResource(R.drawable.ic_material_icon_chevron_up_2)
    } else {
      binding.buttonFilterToggle.setIconResource(R.drawable.ic_material_icon_chevron)
    }
    binding.executePendingBindings()
  }

  /**
   * Clear up
   */
  fun unBind() {
    binding.title = ""
    binding.isSelected = false
    binding.executePendingBindings()
  }

  companion object {

    /**
     * Factory function to create [FilterHeaderItemViewHolder]
     */
    fun create(parent: ViewGroup, layoutId: Int): FilterHeaderItemViewHolder =
      FilterHeaderItemViewHolder(
        DataBindingUtil.inflate(
          LayoutInflater.from(parent.context),
          layoutId, parent, false
        )
      )
  }
}
