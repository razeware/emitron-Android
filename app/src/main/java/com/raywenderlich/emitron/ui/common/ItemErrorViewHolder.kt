package com.raywenderlich.emitron.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.databinding.ItemErrorBinding
import com.raywenderlich.emitron.utils.UiStateManager

/**
 * View holder for error
 */
class ItemErrorViewHolder(
  private val viewDataBinding: ItemErrorBinding,
  private val retryCallback: () -> Unit
) : RecyclerView.ViewHolder(viewDataBinding.root) {

  init {
    viewDataBinding.buttonRetry.setOnClickListener {
      retryCallback()
    }
    viewDataBinding.textViewProgress.visibility = View.GONE
    viewDataBinding.buttonRetry.visibility = View.GONE
  }

  /**
   * @param uiState for this item layout
   * @param hasAppliedFilters If the user has applied filters, the error text is different.
   */
  fun bindTo(uiState: UiStateManager.UiState?, hasAppliedFilters: Boolean) {
    viewDataBinding.progressBar.visibility =
      toVisibility(uiState == UiStateManager.UiState.LOADING)
    viewDataBinding.textViewProgress.visibility =
      toVisibility(uiState == UiStateManager.UiState.LOADING)
    viewDataBinding.buttonRetry.visibility =
      toVisibility(uiState?.hasError() == true)
    viewDataBinding.textViewError.visibility =
      toVisibility(uiState?.hasError() == true)
    when (uiState) {
      UiStateManager.UiState.ERROR_CONNECTION -> viewDataBinding.textViewError.text =
        viewDataBinding.root.resources.getString(R.string.error_no_internet)
      UiStateManager.UiState.ERROR_EMPTY -> {
        viewDataBinding.textViewError.text =
          viewDataBinding.root.resources.getString(R.string.error_library_no_content)
        viewDataBinding.textViewErrorBody.text =
          viewDataBinding.root.resources.getString(R.string.error_library_no_content_body)
        viewDataBinding.textViewErrorBody.visibility = toVisibility(hasAppliedFilters)
        viewDataBinding.buttonRetry.visibility = toVisibility(!hasAppliedFilters)
      }
      else -> viewDataBinding.textViewError.text =
        viewDataBinding.root.resources.getString(R.string.error_generic)
    }
  }

  companion object {
    /**
     * Factory function to create [ItemErrorViewHolder]
     */
    fun create(parent: ViewGroup, layoutId: Int, retryCallback: () -> Unit): ItemErrorViewHolder =
      ItemErrorViewHolder(
        DataBindingUtil.inflate(
          LayoutInflater.from(parent.context),
          layoutId, parent, false
        ), retryCallback
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
