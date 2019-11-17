package com.razeware.emitron.ui.settings

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.razeware.emitron.R

/**
 * Settings bottom sheet to show settings option
 */
class SettingsBottomSheetAdapter(
  private val items: List<Pair<String, Boolean>>,
  private val onItemSelected: (Int) -> Unit
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  /**
   * [RecyclerView.Adapter.getItemViewType]
   *
   * R.layout.item_recent_search_header -> to show recent search item header
   */
  override fun getItemViewType(position: Int): Int {
    return R.layout.item_settings_bottomsheet
  }

  /**
   * [RecyclerView.Adapter.onCreateViewHolder]
   */
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      R.layout.item_settings_bottomsheet -> SettingsBottomSheetItemViewHolder.create(
        parent,
        viewType
      )
      else -> {
        throw IllegalStateException()
      }
    }
  }

  /**
   * [RecyclerView.Adapter.getItemCount]
   */
  override fun getItemCount(): Int = items.size

  /**
   * [RecyclerView.Adapter.onBindViewHolder]
   */
  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder) {
      is SettingsBottomSheetItemViewHolder -> {
        val (option, checked) = items[position]
        holder.bindTo(option, checked) { clickPosition ->
          onItemSelected(clickPosition)
        }
      }
      else -> {
        // No such case
      }
    }
  }
}
