package com.raywenderlich.emitron.ui.content

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.ui.common.ItemErrorViewHolder
import com.raywenderlich.emitron.ui.common.ItemFooterViewHolder
import com.raywenderlich.emitron.ui.common.PagedAdapter
import com.raywenderlich.emitron.utils.Log
import com.raywenderlich.emitron.utils.NetworkState
import com.raywenderlich.emitron.utils.UiStateManager

/**
 * Paged list Adapter for [Data] items
 */
class ContentAdapter(
  private val onItemClick: (Data?) -> Unit,
  private val retryItemCallback: () -> Unit,
  private val retryCallback: () -> Unit,
  private val emptyCallback: (() -> Unit)? = null,
  private val pagedAdapter: PagedAdapter,
  private var contentAdapterType: ContentAdapterType = ContentAdapterType.Content
) : PagedListAdapter<Data, RecyclerView.ViewHolder>(DataDiffCallback) {

  /**
   * Meta data for items
   */
  var included: List<Data>? = null

  /**
   * Content adapter is shared across screens
   *
   * The enum class will represent the view type to which adapter is attached
   *
   * This will help in showing error/empty messages respective to a view
   */
  enum class ContentAdapterType {
    /**
     * Adapter is part of library view
     */
    Content,
    /**
     * Adapter is part of library view with filters applied
     */
    ContentWithFilters,
    /**
     * Adapter is part of library view with search applied
     */
    ContentWithSearch,
    /**
     * Adapter is part of Bookmark view
     */
    Bookmark,
    /**
     * Adapter is part of Progression view
     */
    Progression,
    /**
     * Adapter is part of Download view
     */
    Download;

    /**
     * Adapter is part of library view with filter or search
     *
     * @return True if filters are applied or user is searching else False
     */
    fun isContentWithFilters(): Boolean = this == ContentWithFilters || this == ContentWithSearch

    /**
     * Adapter is part of library view
     *
     * @return True if adapter is part of library view else False
     */
    fun isContent(): Boolean = this == Content || this == ContentWithFilters
  }

  /**
   * [RecyclerView.Adapter.getItemViewType]
   *
   * R.layout.item_error -> to show error without any data is loaded
   * R.layout.item_library_footer -> to show progress/error after some data is loaded
   * R.layout.item_library -> to show any item
   */
  override fun getItemViewType(position: Int): Int {
    return if (pagedAdapter.hasExtraRow() && position == itemCount - 1) {
      if (pagedAdapter.hasUiStateError()) {
        R.layout.item_error
      } else {
        R.layout.item_library_footer
      }
    } else {
      R.layout.item_library
    }
  }

  /**
   * [RecyclerView.Adapter.onCreateViewHolder]
   */
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      R.layout.item_library_footer -> ItemFooterViewHolder.create(
        parent,
        viewType,
        retryItemCallback
      )
      R.layout.item_error -> ItemErrorViewHolder.create(
        parent,
        viewType,
        retryCallback,
        emptyCallback
      )
      R.layout.item_library -> {
        ContentItemViewHolder.create(
          parent,
          viewType
        )
      }
      else -> throw IllegalArgumentException("unknown view type $viewType")
    }
  }

  /**
   * [RecyclerView.Adapter.onBindViewHolder]
   */
  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder) {
      is ContentItemViewHolder -> {
        bindContentItem(holder, position)
      }
      is ItemFooterViewHolder -> holder.bindTo(pagedAdapter.networkState)
      is ItemErrorViewHolder -> holder.bindTo(pagedAdapter.uiState, contentAdapterType)
    }
  }

  /**
   * [RecyclerView.Adapter.onBindViewHolder]
   */
  override fun onBindViewHolder(
    holder: RecyclerView.ViewHolder,
    position: Int,
    payloads: List<Any>
  ) {
    if (payloads.isNotEmpty()) {
      bindContentItem(holder as ContentItemViewHolder, position)
    } else {
      onBindViewHolder(holder, position)
    }
  }

  private fun bindContentItem(viewHolder: ContentItemViewHolder, position: Int) {
    val data = getItem(position)?.setIncluded(included)
    if (data != null) {
      (viewHolder).bindTo(data, contentAdapterType) {
        onItemClick(data)
      }
    } else {
      Log.exception(IllegalArgumentException("Item was null!"))
    }
  }

  /**
   * [RecyclerView.Adapter.onViewRecycled]
   *
   * Clear up on recycle
   */
  override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
    super.onViewRecycled(holder)
    (holder as? ContentItemViewHolder)?.unBind()
  }

  /**
   * [RecyclerView.Adapter.getItemCount]
   *
   * @return If adapter has error/progress return total item count + 1, else total item count
   */
  override fun getItemCount(): Int {
    return super.getItemCount() + if (pagedAdapter.hasExtraRow()) 1 else 0
  }

  private fun notifyItemUpdatedAtLastPosition(itemRemoved: Boolean) {
    if (itemRemoved) {
      notifyItemRemoved(super.getItemCount())
    } else {
      notifyItemInserted(super.getItemCount())
    }
  }

  /**
   * Update UI on [NetworkState] change
   */
  fun updateNetworkState(newNetworkState: NetworkState?) {
    pagedAdapter.updateNetworkState(
      itemCount,
      newNetworkState,
      ::notifyItemChanged,
      ::notifyItemUpdatedAtLastPosition
    )
  }

  /**
   * Update UI on [UiStateManager.UiState] change
   */
  fun updateUiState(newUiState: UiStateManager.UiState?) {
    pagedAdapter.updateUiState(
      itemCount,
      newUiState,
      ::notifyItemChanged,
      ::notifyItemUpdatedAtLastPosition
    )
  }

  /**
   * Update UI [UiStateManager.UiState] for error
   */
  fun updateErrorState(uiState: UiStateManager.UiState?) {
    if (pagedAdapter.errorState != uiState) {
      pagedAdapter.errorState = uiState
      updateUiState(uiState)
    }
  }

  /**
   * Update content adapter type
   *
   * @param type Current parent view type for adapter
   */
  fun updateContentType(type: ContentAdapterType = ContentAdapterType.Content) {
    contentAdapterType = type
  }

  /**
   * Get item for given view holder
   *
   * @param viewHolder ViewHolder to be removed
   */
  fun getItemFor(viewHolder: RecyclerView.ViewHolder): Data? {
    return viewHolder.adapterPosition.run {
      notifyItemRemoved(this)
      getItem(this)
    }
  }

}

/**
 * DiffUtil.ItemCallback for [Data]
 */
object DataDiffCallback : DiffUtil.ItemCallback<Data>() {

  /**
   * [DiffUtil.ItemCallback.areItemsTheSame]
   */
  override fun areItemsTheSame(oldContent: Data, newContent: Data): Boolean =
    oldContent == newContent

  /**
   * [DiffUtil.ItemCallback.areContentsTheSame]
   */
  override fun areContentsTheSame(oldContent: Data, newContent: Data): Boolean =
    oldContent == newContent
}
