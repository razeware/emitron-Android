package com.razeware.emitron.ui.collection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.razeware.emitron.R
import com.razeware.emitron.databinding.ItemCollectionEpisodeBinding
import com.razeware.emitron.model.Data

/**
 * View holder for Episode item in Collection detail view
 */
class CollectionEpisodeItemViewHolder(private val binding: ItemCollectionEpisodeBinding) :
  RecyclerView.ViewHolder(binding.root) {

  /**
   * @param episode [Data] for this item layout
   * @param position User readable episode position
   * @param isPlaybackAllowed true, If the episode requires subscription else false
   * @param onEpisodeSelected Handle episode selection/tap
   * @param onEpisodeCompleted Handle episode marked completed/in-progress
   */
  fun bindTo(
    episode: Data?,
    isDownloadAvailable: Boolean,
    position: Int,
    isPlaybackAllowed: Boolean,
    onEpisodeSelected: (Int) -> Unit,
    onEpisodeCompleted: (Int) -> Unit,
    onEpisodeDownload: (Int) -> Unit
  ) {

    with(binding) {
      root.setOnClickListener {
        onEpisodeSelected(adapterPosition)
      }
      buttonCollectionEpisodeClick.setOnClickListener {
        onEpisodeCompleted(adapterPosition)
      }
      buttonCollectionEpisodeDownload.setOnClickListener {
        onEpisodeDownload(adapterPosition)
      }
      buttonCollectionEpisodeDownload.updateDownloadState(episode?.download)
      data = episode
      this.isDownloadAvailable = isDownloadAvailable
      episodePosition = episode?.getEpisodeNumber(position, isPlaybackAllowed)

      if (isPlaybackAllowed) {
        checkEpisodeCompleted(episode?.isProgressionFinished() ?: false)
      } else {
        setEpisodeLocked()
      }

      progressCompletion.progress = episode?.getProgressionPercentComplete() ?: 0
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

  private fun setEpisodeLocked() {
    with(binding) {
      buttonCollectionEpisode.setIconResource(R.drawable.ic_material_icon_padlock)
      buttonCollectionEpisode.setIconTintResource(R.color.colorIcon)
      buttonCollectionEpisode.isEnabled = false
    }
  }

  private fun markEpisodeComplete() {
    with(binding) {
      buttonCollectionEpisode.setIconResource(R.drawable.ic_material_icon_checkmark_2)
      buttonCollectionEpisode.setIconTintResource(R.color.colorIconInverse)
      buttonCollectionEpisode.setBackgroundColor(
        ContextCompat.getColor(
          root.context,
          R.color.colorPrimary
        )
      )
    }
  }

  private fun markEpisodeInProgress() {
    with(binding) {
      buttonCollectionEpisode.icon = null
      buttonCollectionEpisode.isEnabled = true
      buttonCollectionEpisode.setBackgroundColor(
        ContextCompat.getColor(
          root.context,
          R.color.colorSurfaceDark
        )
      )
    }
  }

  companion object {

    /**
     * Factory function to create [CollectionEpisodeItemViewHolder]
     */
    fun create(parent: ViewGroup, layoutId: Int): CollectionEpisodeItemViewHolder =
      CollectionEpisodeItemViewHolder(
        DataBindingUtil.inflate(
          LayoutInflater.from(parent.context),
          layoutId, parent, false
        )
      )
  }
}
