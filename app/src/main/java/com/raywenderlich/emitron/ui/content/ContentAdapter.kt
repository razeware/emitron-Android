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
  private val pagedAdapter: PagedAdapter
) : PagedListAdapter<Data, RecyclerView.ViewHolder>(DataDiffCallback) {

  /**
   * Meta data for items
   */
  var included: List<Data>? = null

  /**
   * true the user has applied filters
   */
  var hasAppliedFilters: Boolean = false
    private set

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
        retryCallback
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
        val data = getItem(position)?.setIncluded(included)
        if (data != null) {
          data.setReadableReleaseAtWithTypeAndDuration(holder.itemView.context, data)
          (holder).bindTo(data) {
            onItemClick(data)
          }
        } else {
          Log.exception(IllegalArgumentException("Item was null!"))
        }
      }
      is ItemFooterViewHolder -> holder.bindTo(pagedAdapter.networkState)
      is ItemErrorViewHolder -> holder.bindTo(pagedAdapter.uiState, hasAppliedFilters)
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
      val data = getItem(position)?.setIncluded(included)
      if (data != null) {
        data.setReadableReleaseAtWithTypeAndDuration(holder.itemView.context, data)
        (holder as ContentItemViewHolder).bindTo(data) {
          onItemClick(data)
        }
      } else {
        Log.exception(IllegalArgumentException("Item was null!"))
      }
    } else {
      onBindViewHolder(holder, position)
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
   * Apply filters
   *
   * @param hasApplied if user has applied filters
   */
  fun hasAppliedFilters(hasApplied: Boolean = true) {
    hasAppliedFilters = hasApplied
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
