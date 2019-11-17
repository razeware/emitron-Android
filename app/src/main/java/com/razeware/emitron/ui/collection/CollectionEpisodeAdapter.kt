package com.razeware.emitron.ui.collection

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.razeware.emitron.R
import com.razeware.emitron.model.Data

/**
 * Adapter for collection episodes
 */
class CollectionEpisodeAdapter(
  private val onEpisodeSelected: (Data?, Data?) -> Unit,
  /**
   * Handle to mark episode complete/in-progress
   */
  private val onEpisodeCompleted: (Data?, Int) -> Unit,
  private val onEpisodeDownload: (Data?, Int) -> Unit
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val items: MutableList<EpisodeItem> = mutableListOf()

  private var bindHeaderCount = -1

  /**
   * Property to check If the collection playback is allowed,
   * If playback is not allowed a lock icon will be shown and collection won't be playable.
   *
   */
  private var isContentPlaybackAllowed: Boolean = false

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
    return !items[position].title.isNullOrBlank()
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
  override fun getItemCount(): Int = if (items.isNotEmpty()) items.size else 0

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
    val contentEpisode = items[position]
    contentEpisode.title?.let {
      viewHolder.bindTo(it)
    }
    bindHeaderCount += 1
  }

  private fun bindItem(viewHolder: CollectionEpisodeItemViewHolder, position: Int) {

    val (_, data, cachedEpisodePosition) = items[position]

    val episodePosition = if (cachedEpisodePosition == 0) {
      val newCachedPosition = position - bindHeaderCount
      items[position] = EpisodeItem(data = data, position = newCachedPosition)
      newCachedPosition
    } else {
      cachedEpisodePosition
    }

    viewHolder.bindTo(data, episodePosition, isContentPlaybackAllowed, { selectedPosition ->
      val contentEpisode = items[selectedPosition]
      val nextContentEpisode = if (selectedPosition < items.size - 1) {
        items[selectedPosition + 1]
      } else {
        EpisodeItem()
      }
      onEpisodeSelected(contentEpisode.data, nextContentEpisode.data)
    }, { selectedPosition ->
      val episode = items[selectedPosition]
      val updatedEpisode = episode.data?.toggleProgressionFinished()
      items[selectedPosition] = episode.copy(data = updatedEpisode)
      onEpisodeCompleted(episode.data, selectedPosition)
      notifyItemChanged(selectedPosition)
    }, { selectedPosition ->
      val contentEpisode = items[selectedPosition]
      onEpisodeDownload(contentEpisode.data, selectedPosition)
    })
  }

  /**
   * Add/Submit episode list
   */
  fun submitList(it: List<EpisodeItem>) {
    this.items.clear()
    this.items.addAll(it)
    notifyDataSetChanged()
  }

  /**
   * Update the respective episode UI, after episode is marked completed and the API request fails
   */
  fun updateEpisodeCompletion(position: Int) {
    val contentEpisode = items[position]
    items[position] = contentEpisode.copy(data = contentEpisode.data?.toggleFinished())
    notifyItemChanged(position)
  }

  /**
   * Update episode download progress
   *
   * @param downloads Downloads in progress
   */
  fun updateEpisodeDownloadProgress(
    downloads:
    List<com.razeware.emitron.model.entity.Download>
  ) {
    downloads.forEach { download ->
      val position = items.indexOfFirst { it.data?.id == download.downloadId }
      if (position != -1) {
        val contentEpisode = items[position]
        val updateEpisodeData =
          contentEpisode.data?.updateDownloadProgress(download.toDownloadState())
        items[position] = contentEpisode.copy(data = updateEpisodeData)
        notifyItemChanged(position)
      }
    }
  }

  /**
   * Remove episode download
   *
   * @param downloads Download ids to be removed
   */
  fun removeEpisodeDownload(
    downloads: List<String>
  ) {
    downloads.forEach { downloadId ->
      val position = items.indexOfFirst { it.data?.id == downloadId }
      if (position != -1) {
        val contentEpisode = items[position]
        val updateEpisodeData = contentEpisode.data?.removeDownload()
        items[position] = contentEpisode.copy(data = updateEpisodeData)
        notifyItemChanged(position)
      }
    }
  }

  /**
   * Update playback allowed for adapter content
   */
  fun updateContentPlaybackAllowed(
    contentPlaybackAllowed: Boolean,
    refresh: Boolean = false
  ) {
    this.isContentPlaybackAllowed = contentPlaybackAllowed
    if (refresh) {
      notifyDataSetChanged()
    }
  }
}

/**
 * View object for episode item
 *
 * This may represent an episode or a collection header
 *
 */
data class EpisodeItem(
  /**
   * Episode collection header
   */
  val title: String? = "",
  /**
   * Episode item data [Data]
   */
  val data: Data? = null,
  /**
   * Episode position
   *
   * You are caching episode position to update the episode UI after an API request.
   */
  val position: Int = 0
) {

  companion object {

    private fun getContentItems(data: Data): List<EpisodeItem> =
      data.getChildContents().map { childData ->
        EpisodeItem(data = childData)
      }

    private fun getTitle(data: Data): EpisodeItem? {
      val name = data.getName()
      return if (!name.isNullOrBlank()) {
        EpisodeItem(data.getName())
      } else {
        null
      }
    }

    /**
     * Build episode item from [Data]
     */
    fun buildFrom(data: Data): List<EpisodeItem> =
      listOfNotNull(getTitle(data), *(getContentItems(data).toTypedArray()))

  }
}
