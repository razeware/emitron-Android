package com.razeware.emitron.ui.content

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.razeware.emitron.R
import com.razeware.emitron.model.Data
import com.razeware.emitron.ui.common.ItemErrorViewHolder
import com.razeware.emitron.ui.common.ItemFooterViewHolder
import com.razeware.emitron.ui.common.PagedAdapter
import com.razeware.emitron.utils.Log
import com.razeware.emitron.utils.NetworkState
import com.razeware.emitron.utils.UiStateManager

/**
 * Paged list Adapter for [Data] items
 */
class ContentAdapter private constructor(
  private var adapterContentType: AdapterContentType = AdapterContentType.Content,
  private val pagedAdapter: PagedAdapter,
  private val onItemClick: (Data?) -> Unit,
  private val onItemRetry: () -> Unit,
  private val retryCallback: () -> Unit,
  private val emptyCallback: (() -> Unit)? = null,
  private val bookmarkCallback: ((Data?) -> Unit)? = null,
  private val downloadCallback: ((Data?, Int) -> Unit)? = null
) : PagedListAdapter<Data, RecyclerView.ViewHolder>(DataDiffCallback) {

  companion object {

    /**
     * Factory function for [ContentAdapter]
     */
    fun build(
      adapterContentType: AdapterContentType = AdapterContentType.Content,
      pagedAdapter: PagedAdapter = PagedAdapter(),
      onItemClick: (Data?) -> Unit,
      onItemRetry: () -> Unit,
      retryCallback: () -> Unit,
      emptyCallback: (() -> Unit)? = null,
      bookmarkCallback: ((Data?) -> Unit)? = null,
      downloadCallback: ((Data?, Int) -> Unit)? = null
    ): ContentAdapter = ContentAdapter(
      adapterContentType = adapterContentType,
      pagedAdapter = pagedAdapter,
      onItemClick = onItemClick,
      onItemRetry = onItemRetry,
      retryCallback = retryCallback,
      emptyCallback = emptyCallback,
      bookmarkCallback = bookmarkCallback,
      downloadCallback = downloadCallback
    )
  }

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
  enum class AdapterContentType {
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
    ContentBookmarked,
    /**
     * Adapter is part of Progression view
     */
    ContentInProgress,
    /**
     * Adapter is part of Progression view
     */
    ContentCompleted,
    /**
     * Adapter is part of Download view
     */
    ContentDownloaded;

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
    fun isContent(): Boolean =
      this == Content || this == ContentWithFilters || this == ContentWithSearch

    /**
     * Adapter is part of bookmark view
     *
     * @return True if adapter is part of bookmark view else False
     */
    fun isBookmarked(): Boolean =
      this == ContentBookmarked

    /**
     * Adapter is part of completed progression view
     *
     * @return True if adapter is part of completed progression view else False
     */
    fun isCompleted(): Boolean =
      this == ContentCompleted

    /**
     * Adapter is part of downloads view
     *
     * @return True if adapter is part of downloads view else False
     */
    fun isDownloaded(): Boolean =
      this == ContentDownloaded
  }

  /**
   * [RecyclerView.Adapter.getItemViewType]
   *
   * R.layout.item_error -> to show error without any data is loaded
   * R.layout.item_footer -> to show progress/error after some data is loaded
   * R.layout.item_content -> to show any item
   */
  override fun getItemViewType(position: Int): Int {
    return if (pagedAdapter.hasExtraRow() && position == itemCount - 1) {
      if (pagedAdapter.hasUiStateError()) {
        R.layout.item_error
      } else {
        R.layout.item_footer
      }
    } else {
      R.layout.item_content
    }
  }

  /**
   * [RecyclerView.Adapter.onCreateViewHolder]
   */
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      R.layout.item_footer -> ItemFooterViewHolder.create(
        parent,
        viewType,
        onItemRetry
      )
      R.layout.item_error -> ItemErrorViewHolder.create(
        parent,
        viewType,
        retryCallback,
        emptyCallback
      )
      R.layout.item_content -> {
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
      is ItemErrorViewHolder -> holder.bindTo(pagedAdapter.uiState, adapterContentType)
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
    val data = getItem(position)?.updateRelationships(included)
    if (null != data) {
      (viewHolder).bindTo(
        content = data,
        adapterContent = adapterContentType,
        onItemClick = { clickPosition ->
          onItemClick(getItem(clickPosition))
        },
        bookmarkCallback = { clickPosition ->
          bookmarkCallback?.invoke(getItem(clickPosition))
        },
        downloadCallback = { clickPosition, status ->
          downloadCallback?.invoke(getItem(clickPosition), status)
        })
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
   * Update content adapter contentType
   *
   * @param contentType Current parent view contentType for adapter
   */
  fun updateContentType(contentType: AdapterContentType = AdapterContentType.Content) {
    adapterContentType = contentType
  }

  /**
   * Get item for given view holder
   *
   * @param position Adapter position
   */
  fun getItemFor(position: Int): Data? {
    return position.run {
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