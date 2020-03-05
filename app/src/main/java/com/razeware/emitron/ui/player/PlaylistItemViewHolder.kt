package com.razeware.emitron.ui.player

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.razeware.emitron.R
import com.razeware.emitron.databinding.ItemPlaylistBinding
import com.razeware.emitron.model.Data

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

    with(binding) {
      root.setOnClickListener {
        onEpisodeSelected(adapterPosition)
      }
      episodePosition = (position + 1).toString()
      data = episode

      progressCompletion.progress = episode?.getProgressionPercentComplete() ?: 0
      checkEpisodeCompleted(episode?.isProgressionFinished() ?: false)
      progressCompletion.isVisible =
        episode?.isProgressionFinished() != true
            && episode?.getProgressionPercentComplete() != 0
      collectionItemDivider.isVisible =
        episode?.isProgressionFinished() == true ||
            episode?.getProgressionPercentComplete() == 0
      executePendingBindings()
    }
  }

  private fun checkEpisodeCompleted(isContentFinished: Boolean) {
    if (isContentFinished) {
      markEpisodeComplete()
    } else {
      markEpisodeInProgress()
    }
  }

  private fun markEpisodeComplete() {
    with(binding) {
      buttonPlayerEpisode.setIconResource(R.drawable.ic_material_icon_checkmark)
      buttonPlayerEpisode.setIconTintResource(R.color.colorIconInverse)
      buttonPlayerEpisode.setBackgroundColor(
        ContextCompat.getColor(
          root.context,
          R.color.colorPrimary
        )
      )
    }
  }

  private fun markEpisodeInProgress() {
    with(binding) {
      buttonPlayerEpisode.icon = null
      buttonPlayerEpisode.isEnabled = true
      buttonPlayerEpisode.setBackgroundColor(
        ContextCompat.getColor(
          root.context,
          R.color.colorSurfaceDark
        )
      )
    }
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
