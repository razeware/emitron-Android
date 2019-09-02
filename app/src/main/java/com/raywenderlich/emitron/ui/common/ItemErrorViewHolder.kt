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
   * @param contentAdapterType Type for current adapter.
   */
  fun bindTo(
    uiState: UiStateManager.UiState?,
    contentAdapterType: ContentAdapter.ContentAdapterType
  ) {

    with(viewDataBinding) {
      progressBar.visibility =
        toVisibility(uiState == UiStateManager.UiState.LOADING)
      textViewProgress.visibility =
        toVisibility(uiState == UiStateManager.UiState.LOADING)
      textViewError.visibility =
        toVisibility(uiState?.hasError() == true)
      buttonRetry.visibility =
        toVisibility(uiState?.hasError() == true)
      viewDataBinding.buttonRetry.setOnClickListener {
        if (uiState?.isEmpty() == true) {
          emptyCallback?.invoke()
        } else {
          retryCallback()
        }
      }

      viewDataBinding.buttonRetry.setIconResource(
        getRetryActionIconResource(
          contentAdapterType
        )
      )

      when (uiState) {
        UiStateManager.UiState.ERROR_CONNECTION -> viewDataBinding.textViewError.text =
          viewDataBinding.root.resources.getString(R.string.error_no_internet)
        UiStateManager.UiState.ERROR_EMPTY -> {
          textViewError.text = getEmptyErrorForAdapterType(contentAdapterType)
          textViewErrorBody.text =
            viewDataBinding.root.resources.getString(R.string.error_library_no_content_body)
          textViewErrorBody.visibility =
            toVisibility(contentAdapterType.isContentWithFilters())
          buttonRetry.visibility =
            toVisibility(!contentAdapterType.isContentWithFilters())
          buttonRetry.text =
            getRetryButtonLabelForAdapterType(contentAdapterType)
        }
        else -> textViewError.text =
          viewDataBinding.root.resources.getString(R.string.error_generic)
      }
    }
  }

  private fun getEmptyErrorForAdapterType(contentAdapterType: ContentAdapter.ContentAdapterType) =
    when (contentAdapterType) {
      ContentAdapter.ContentAdapterType.Content,
      ContentAdapter.ContentAdapterType.ContentWithFilters,
      ContentAdapter.ContentAdapterType.ContentWithSearch ->
        viewDataBinding.root.resources.getString(R.string.error_library_no_content)
      ContentAdapter.ContentAdapterType.Bookmark ->
        viewDataBinding.root.resources.getString(R.string.body_bookmarks_empty)
      ContentAdapter.ContentAdapterType.Progression ->
        viewDataBinding.root.resources.getString(R.string.body_progressions_empty)
      ContentAdapter.ContentAdapterType.Download ->
        viewDataBinding.root.resources.getString(R.string.body_bookmarks_empty)
    }

  private fun getRetryButtonLabelForAdapterType(
    contentAdapterType:
    ContentAdapter.ContentAdapterType
  ) =
    when (contentAdapterType) {
      ContentAdapter.ContentAdapterType.ContentWithSearch,
      ContentAdapter.ContentAdapterType.Content,
      ContentAdapter.ContentAdapterType.ContentWithFilters ->
        viewDataBinding.root.resources.getString(R.string.button_retry)
      ContentAdapter.ContentAdapterType.Bookmark,
      ContentAdapter.ContentAdapterType.Progression,
      ContentAdapter.ContentAdapterType.Download ->
        viewDataBinding.root.resources.getString(R.string.button_explore_tutorials)
    }

  private fun getRetryActionIconResource(
    contentAdapterType:
    ContentAdapter.ContentAdapterType
  ) =
    when (contentAdapterType) {
      ContentAdapter.ContentAdapterType.ContentWithSearch,
      ContentAdapter.ContentAdapterType.Content,
      ContentAdapter.ContentAdapterType.ContentWithFilters -> 0
      ContentAdapter.ContentAdapterType.Bookmark,
      ContentAdapter.ContentAdapterType.Progression,
      ContentAdapter.ContentAdapterType.Download ->
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

    /**
     * Return visibility based on constraint
     *
     * @param constraint True/False
     *
     * @return [View.VISIBLE] if constraint is true, else [View.GONE]
     */
    fun toVisibility(constraint: Boolean): Int = if (constraint) {
      View.VISIBLE
    } else {
      View.GONE
    }
  }
}
