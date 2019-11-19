package com.razeware.emitron.ui.player

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.razeware.emitron.R
import com.razeware.emitron.model.Data

/**
 * Settings bottom sheet to show settings option
 */
class PlaylistBottomSheetAdapter(
  private val items: List<Data>,
  private val onItemSelected: (Int) -> Unit
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  /**
   * [RecyclerView.Adapter.getItemViewType]
   *
   * R.layout.item_recent_search_header -> to show recent search item header
   */
  override fun getItemViewType(position: Int): Int {
    return R.layout.item_playlist
  }

  /**
   * [RecyclerView.Adapter.onCreateViewHolder]
   */
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      R.layout.item_playlist -> PlaylistItemViewHolder.create(
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
      is PlaylistItemViewHolder -> {
        val item = items[position]
        holder.bindTo(position, item) { clickPosition ->
          onItemSelected(clickPosition)
        }
      }
      else -> {
        // No such case
      }
    }
  }
}
