package com.razeware.emitron.ui.collection

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.razeware.emitron.R
import com.razeware.emitron.model.Data
import com.razeware.emitron.utils.extensions.isNetConnected

/**
 * Adapter for collection episodes
 */
class CollectionEpisodeAdapter(
  private val onEpisodeSelected: (Data?, Data?) -> Unit,
  /**
   * Handle to mark episode complete/in-progress
   */
  private val onEpisodeCompleted: (Data?, Int) -> Unit,
  private val onEpisodeDownload: (Data?, Int) -> Unit,
  private val viewModel: CollectionViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val differ by lazy { AsyncListDiffer(this, CollectionEpisodeDiffCallback()) }

  /**
   * [RecyclerView.Adapter.getItemViewType]
   *
   * R.layout.item_collection_episode_header -> to show episode collection header
   * R.layout.item_collection_episode -> to show episode UI
   */
  override fun getItemViewType(position: Int): Int {
    if (isHeaderPosition(position))
      return R.layout.item_collection_episode_header

    return R.layout.item_collection_episode
  }

  private fun isHeaderPosition(position: Int): Boolean {
    return !viewModel.getCollectionEpisodes()[position].title.isNullOrBlank()
  }

  /**
   * See [RecyclerView.Adapter.onCreateViewHolder]
   */
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      R.layout.item_collection_episode -> CollectionEpisodeItemViewHolder.create(parent, viewType)
      else -> {
        CollectionEpisodeHeaderItemViewHolder.create(parent, viewType)
      }
    }
  }

  /**
   * See [RecyclerView.Adapter.getItemCount]
   */
  override fun getItemCount(): Int =
    if (viewModel.getCollectionEpisodes().isNotEmpty())
      viewModel.getCollectionEpisodes().size
    else 0

  /**
   * See [RecyclerView.Adapter.onBindViewHolder]
   */
  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder) {
      is CollectionEpisodeItemViewHolder -> {
        bindItem(holder, position)
      }
      is CollectionEpisodeHeaderItemViewHolder -> {
        bindHeaderItem(holder, position)
      }
      else -> {
        // No such case
      }
    }
  }

  private fun bindHeaderItem(viewHolder: CollectionEpisodeHeaderItemViewHolder, position: Int) {
    val contentEpisode = viewModel.getCollectionEpisodes()[position]
    viewHolder.bindTo(contentEpisode.title)
  }

  private fun bindItem(viewHolder: CollectionEpisodeItemViewHolder, position: Int) {

    val episodes = viewModel.getCollectionEpisodes()
    val (_, data, episodePosition) = episodes[position]

    val hasConnection = viewHolder.itemView.context.isNetConnected()
    viewHolder.bindTo(
      episode = data,
      isDownloadAvailable = viewModel.isDownloadAllowed(),
      position = episodePosition,
      isPlaybackAllowed = viewModel.isContentPlaybackAllowed(hasConnection),
      onEpisodeSelected = { selectedPosition ->
        val contentEpisode = episodes[selectedPosition]
        val nextContentEpisode = if (selectedPosition < episodes.size - 1) {
          episodes[selectedPosition + 1]
        } else {
          CollectionEpisode()
        }
        onEpisodeSelected(contentEpisode.data, nextContentEpisode.data)
      },
      onEpisodeCompleted = { selectedPosition ->
        val episode = viewModel.toggleEpisodeCompletion(selectedPosition)
        onEpisodeCompleted(episode, selectedPosition)
        notifyItemChanged(selectedPosition)
      },
      onEpisodeDownload = { selectedPosition ->
        val contentEpisode = episodes[selectedPosition]
        onEpisodeDownload(contentEpisode.data, selectedPosition)
      })
  }

  /**
   * Update items using [DiffUtil]
   */
  fun update(list: List<CollectionEpisode>?) {
    differ.submitList(list)
  }
}
