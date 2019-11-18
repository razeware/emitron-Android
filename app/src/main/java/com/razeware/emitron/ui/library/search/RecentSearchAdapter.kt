package com.razeware.emitron.ui.library.search

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.razeware.emitron.R

class RecentSearchAdapter(
  private val recentSearchItems: List<String>,
  private val onItemSelected: (String) -> Unit
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  /**
   * [RecyclerView.Adapter.getItemViewType]
   *
   * R.layout.item_recent_search -> to show recent search item
   * R.layout.item_recent_search_header -> to show recent search item header
   */
  override fun getItemViewType(position: Int): Int {
    if (position == 0)
      return R.layout.item_recent_search_header

    return R.layout.item_recent_search
  }

  /**
   * [RecyclerView.Adapter.onCreateViewHolder]
   */
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      R.layout.item_recent_search -> RecentSearchItemViewHolder.create(parent, viewType)
      else -> {
        RecentSearchHeaderItemViewHolder.create(parent, viewType)
      }
    }
  }

  /**
   * [RecyclerView.Adapter.getItemCount]
   */
  override fun getItemCount(): Int = recentSearchItems.size + 1

  /**
   * [RecyclerView.Adapter.onBindViewHolder]
   */
  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder) {
      is RecentSearchItemViewHolder -> {
        val recentSearch = recentSearchItems[position - 1]
        holder.bindTo(recentSearch) { clickPosition ->
          onItemSelected(recentSearchItems[clickPosition - 1])
        }
      }
      else -> {
        // No such case
      }
    }
  }
}
