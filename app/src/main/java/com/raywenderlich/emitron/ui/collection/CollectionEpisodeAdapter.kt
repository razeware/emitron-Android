package com.raywenderlich.emitron.ui.collection

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.model.Data

/**
 * Adapter for collection episodes
 */
class CollectionEpisodeAdapter(
  private val onEpisodeSelected: (Data?, Data?) -> Unit,
  /**
   * Handle to mark episode complete/in-progress
   */
  private val onEpisodeCompleted: (Data?, Int) -> Unit
) :
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val items: MutableList<EpisodeItem> = mutableListOf()

  private var bindHeaderCount = -1

  /**
   * Property to check If the collection requires a subscription
   *
   * This will help you in disabling clicks on episodes, changing the episode UI accordingly.
   */
  var isProCourse: Boolean = false

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

    viewHolder.bindTo(data, episodePosition, isProCourse, { selectedPosition ->
      val contentEpisode = items[selectedPosition]
      val nextContentEpisode = if (selectedPosition < items.size) {
        items[selectedPosition + 1]
      } else {
        EpisodeItem()
      }
      onEpisodeSelected(contentEpisode.data, nextContentEpisode.data)
    }, { selectedPosition ->
      val contentEpisode = items[selectedPosition]
      items[selectedPosition] =
        contentEpisode.copy(data = contentEpisode.data?.toggleFinished())
      onEpisodeCompleted(contentEpisode.data, selectedPosition)
      notifyItemChanged(selectedPosition)
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
      data.getGroupedData().map { childData ->
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
