package com.raywenderlich.emitron.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.databinding.ItemErrorBinding
import com.raywenderlich.emitron.ui.content.ContentAdapter
import com.raywenderlich.emitron.utils.UiStateManager
import com.raywenderlich.emitron.utils.extensions.isNetNotConnected
import com.raywenderlich.emitron.utils.extensions.toVisibility

/**
 * View holder for error
 */
class ItemErrorViewHolder(
  private val viewDataBinding: ItemErrorBinding,
  private val retryCallback: () -> Unit,
  private val emptyCallback: (() -> Unit)?
) : RecyclerView.ViewHolder(viewDataBinding.root) {

  init {
    viewDataBinding.textViewProgress.visibility = View.GONE
    viewDataBinding.buttonRetry.visibility = View.GONE
  }

  /**
   * @param uiState for this item layout
   * @param adapterContentType Type for current adapter.
   */
  fun bindTo(
    uiState: UiStateManager.UiState?,
    adapterContentType: ContentAdapter.AdapterContentType
  ) {

    with(viewDataBinding) {
      progressBar.toVisibility(uiState == UiStateManager.UiState.LOADING)
      textViewProgress.toVisibility(uiState == UiStateManager.UiState.LOADING)
      textViewError.toVisibility(uiState?.hasError() == true)
      buttonRetry.toVisibility(uiState?.hasError() == true)
      viewDataBinding.buttonRetry.setOnClickListener {
        if (uiState?.isEmpty() == true) {
          emptyCallback?.invoke()
        } else {
          retryCallback()
        }
      }

      viewDataBinding.buttonRetry.setIconResource(
        getRetryActionIconResource(
          adapterContentType
        )
      )

      when (uiState) {
        UiStateManager.UiState.ERROR_CONNECTION -> viewDataBinding.textViewError.text =
          viewDataBinding.root.resources.getString(R.string.error_no_internet)
        UiStateManager.UiState.ERROR_EMPTY -> {
          textViewError.text = getEmptyErrorForAdapterType(adapterContentType)
          textViewErrorBody.text =
            viewDataBinding.root.resources.getString(R.string.error_library_no_content_body)
          textViewErrorBody.toVisibility(adapterContentType.isContentWithFilters())
          buttonRetry.toVisibility(!adapterContentType.isContentWithFilters())
          buttonRetry.text =
            getRetryButtonLabelForAdapterType(adapterContentType)
        }
        else -> if (root.context.isNetNotConnected()) {
          textViewError.text =
            viewDataBinding.root.resources.getString(R.string.error_no_internet)
        } else {
          textViewError.text =
            viewDataBinding.root.resources.getString(R.string.error_generic)
        }
      }
    }
  }

  private fun getEmptyErrorForAdapterType(adapterContentType: ContentAdapter.AdapterContentType) =
    when (adapterContentType) {
      ContentAdapter.AdapterContentType.Content,
      ContentAdapter.AdapterContentType.ContentWithFilters,
      ContentAdapter.AdapterContentType.ContentWithSearch ->
        viewDataBinding.root.resources.getString(R.string.error_library_no_content)
      ContentAdapter.AdapterContentType.ContentBookmarked ->
        viewDataBinding.root.resources.getString(R.string.body_bookmarks_empty)
      ContentAdapter.AdapterContentType.ContentInProgress ->
        viewDataBinding.root.resources.getString(R.string.body_progressions_empty)
      ContentAdapter.AdapterContentType.ContentCompleted ->
        viewDataBinding.root.resources.getString(R.string.body_progressions_completed_empty)
      ContentAdapter.AdapterContentType.ContentDownloaded ->
        viewDataBinding.root.resources.getString(R.string.body_bookmarks_empty)
    }

  private fun getRetryButtonLabelForAdapterType(
    adapterContentType:
    ContentAdapter.AdapterContentType
  ) =
    when (adapterContentType) {
      ContentAdapter.AdapterContentType.ContentWithSearch,
      ContentAdapter.AdapterContentType.Content,
      ContentAdapter.AdapterContentType.ContentWithFilters ->
        viewDataBinding.root.resources.getString(R.string.button_retry)
      ContentAdapter.AdapterContentType.ContentBookmarked,
      ContentAdapter.AdapterContentType.ContentCompleted,
      ContentAdapter.AdapterContentType.ContentInProgress,
      ContentAdapter.AdapterContentType.ContentDownloaded ->
        viewDataBinding.root.resources.getString(R.string.button_explore_tutorials)
    }

  private fun getRetryActionIconResource(
    adapterContentType:
    ContentAdapter.AdapterContentType
  ) =
    when (adapterContentType) {
      ContentAdapter.AdapterContentType.ContentWithSearch,
      ContentAdapter.AdapterContentType.Content,
      ContentAdapter.AdapterContentType.ContentWithFilters -> 0
      ContentAdapter.AdapterContentType.ContentBookmarked,
      ContentAdapter.AdapterContentType.ContentCompleted,
      ContentAdapter.AdapterContentType.ContentInProgress,
      ContentAdapter.AdapterContentType.ContentDownloaded ->
        R.drawable.ic_material_button_icon_arrow_right_green_contained
    }

  companion object {
    /**
     * Factory function to create [ItemErrorViewHolder]
     */
    fun create(
      parent: ViewGroup, layoutId: Int,
      retryCallback: () -> Unit,
      emptyCallback: (() -> Unit)?
    ): ItemErrorViewHolder =
      ItemErrorViewHolder(
        DataBindingUtil.inflate(
          LayoutInflater.from(parent.context),
          layoutId, parent, false
        ), retryCallback, emptyCallback
      )
  }
}
