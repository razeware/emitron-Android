package com.raywenderlich.emitron.ui.player

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.emitron.databinding.ItemPlaylistBinding
import com.raywenderlich.emitron.model.Data

/**
 * View holder for playlist item
 */
class PlaylistItemViewHolder(private val binding: ItemPlaylistBinding) :
  RecyclerView.ViewHolder(binding.root) {

  /**
   * @param position String
   * @param episode [Data] for this item layout
   * @param onEpisodeSelected Handle episode selection/tap
   */
  fun bindTo(
    position: Int,
    episode: Data?,
    onEpisodeSelected: (Int) -> Unit
  ) {
    binding.root.setOnClickListener {
      onEpisodeSelected(adapterPosition)
    }
    binding.episodePosition = (position + 1).toString()
    binding.data = episode
    binding.executePendingBindings()
  }

  companion object {

    /**
     * Factory function to create [PlaylistItemViewHolder]
     */
    fun create(parent: ViewGroup, layoutId: Int): PlaylistItemViewHolder =
      PlaylistItemViewHolder(
        DataBindingUtil.inflate(
          LayoutInflater.from(parent.context),
          layoutId, parent, false
        )
      )
  }
}
