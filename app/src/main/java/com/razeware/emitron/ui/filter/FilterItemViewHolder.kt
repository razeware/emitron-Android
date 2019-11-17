package com.razeware.emitron.ui.filter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.razeware.emitron.databinding.ItemFilterBinding

/**
 * View holder for filter options
 */
class FilterItemViewHolder(private val binding: ItemFilterBinding) :
  RecyclerView.ViewHolder(binding.root) {

  /**
   * Bind filter item data
   *
   * @param data title of filter
   * @param isSelected true if filter is selected, else false.
   * @param onItemClick Click listener for this item layout
   */
  fun bindTo(data: String, isSelected: Boolean, onItemClick: (Int, Boolean) -> Unit) {
    unBind()
    binding.root.setOnClickListener {
      onItemClick(
        adapterPosition, binding.checkboxFilterItem.isChecked
      )
    }
    binding.title = data
    binding.checked = isSelected
    binding.executePendingBindings()
  }

  /**
   * Clear up
   */
  fun unBind() {
    binding.title = ""
    binding.checked = false
    binding.executePendingBindings()
  }

  companion object {

    /**
     * Factory function to create [FilterItemViewHolder]
     */
    fun create(parent: ViewGroup, layoutId: Int): FilterItemViewHolder =
      FilterItemViewHolder(
        DataBindingUtil.inflate(
          LayoutInflater.from(parent.context),
          layoutId, parent, false
        )
      )
  }
}
