package com.razeware.emitron.ui.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.razeware.emitron.databinding.ItemSettingsBottomsheetBinding

/**
 * View holder for filter options group header
 */
class SettingsBottomSheetItemViewHolder(private val binding: ItemSettingsBottomsheetBinding) :
  RecyclerView.ViewHolder(binding.root) {

  /**
   * Bind recent search item
   *
   * @param title title of recent search
   * @param onItemClick Click listener for this item layout
   */
  fun bindTo(title: String, checked: Boolean, onItemClick: (Int) -> Unit) {
    binding.root.visibility = View.VISIBLE
    binding.root.setOnClickListener {
      onItemClick(adapterPosition)
    }
    binding.title = title
    binding.checked = checked
    binding.executePendingBindings()
  }

  companion object {

    /**
     * Factory function to create [SettingsBottomSheetItemViewHolder]
     */
    fun create(parent: ViewGroup, layoutId: Int): SettingsBottomSheetItemViewHolder =
      SettingsBottomSheetItemViewHolder(
        DataBindingUtil.inflate(
          LayoutInflater.from(parent.context),
          layoutId, parent, false
        )
      )
  }
}
