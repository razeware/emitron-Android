package com.raywenderlich.emitron.ui.collection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.databinding.ItemCollectionEpisodeBinding
import com.raywenderlich.emitron.model.Data

/**
 * View holder for Episode item in Collection detail view
 */
class CollectionEpisodeItemViewHolder(private val binding: ItemCollectionEpisodeBinding) :
  RecyclerView.ViewHolder(binding.root) {

  /**
   * @param episode [Data] for this item layout
   * @param position User readable episode position
   * @param episodeIsProContent true, If the episode requires subscription else false
   * @param onEpisodeSelected Handle episode selection/tap
   * @param onEpisodeCompleted Handle episode marked completed/in-progress
   */
  fun bindTo(
    episode: Data?,
    position: Int,
    episodeIsProContent: Boolean,
    onEpisodeSelected: (Int) -> Unit,
    onEpisodeCompleted: (Int) -> Unit,
    onEpisodeDownload: (Int) -> Unit
  ) {
    binding.root.setOnClickListener {
      onEpisodeSelected(adapterPosition)
    }
    binding.buttonCollectionEpisode.setOnClickListener {
      onEpisodeCompleted(adapterPosition)
    }
    binding.buttonCollectionEpisodeDownload.setOnClickListener {
      onEpisodeDownload(adapterPosition)
    }
    binding.buttonCollectionEpisodeDownload.updateDownloadState(episode?.download)
    binding.data = episode
    binding.episodePosition = episode?.getEpisodeNumber(position, episodeIsProContent)

    if (episodeIsProContent) {
      setProCourse()
    } else {
      checkEpisodeCompleted(episode?.isFinished() ?: false)
    }

    binding.executePendingBindings()
  }

  private fun checkEpisodeCompleted(isContentFinished: Boolean) {
    if (isContentFinished) {
      markEpisodeComplete()
    } else {
      markEpisodeInProgress()
    }
  }

  private fun setProCourse() {
    binding.buttonCollectionEpisode.setIconResource(R.drawable.ic_material_icon_padlock)
    binding.buttonCollectionEpisode.setIconTintResource(R.color.colorIcon)
    binding.buttonCollectionEpisode.isEnabled = false
  }

  private fun markEpisodeComplete() {
    binding.buttonCollectionEpisode.setIconResource(R.drawable.ic_material_icon_checkmark)
    binding.buttonCollectionEpisode.setIconTintResource(R.color.colorIconInverse)
    binding.buttonCollectionEpisode.setBackgroundColor(
      ContextCompat.getColor(
        binding.root.context,
        R.color.colorPrimary
      )
    )
  }

  private fun markEpisodeInProgress() {
    binding.buttonCollectionEpisode.isEnabled = true
    binding.buttonCollectionEpisode.setBackgroundColor(
      ContextCompat.getColor(
        binding.root.context,
        R.color.colorSurface
      )
    )
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
