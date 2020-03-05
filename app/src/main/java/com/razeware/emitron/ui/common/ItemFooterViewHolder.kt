package com.razeware.emitron.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.razeware.emitron.databinding.ItemFooterBinding
import com.razeware.emitron.utils.UiStateManager
import com.razeware.emitron.utils.hasFailed
import com.razeware.emitron.utils.isLoading

/**
 * View holder for footer progress/error
 */
class ItemFooterViewHolder(
  private val viewDataBinding: ItemFooterBinding,
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
   * @param uiState Updated UI state
   */
  fun bindTo(uiState: UiStateManager.UiState?) {
    viewDataBinding.progressBar.isVisible = uiState.isLoading()
    viewDataBinding.textViewProgress.isVisible = uiState.isLoading()
    viewDataBinding.buttonRetry.isVisible = uiState.hasFailed()
  }

  companion object {

    /**
     * Factory function to create [ItemFooterViewHolder]
     */
    fun create(parent: ViewGroup, layoutId: Int, retryCallback: () -> Unit): ItemFooterViewHolder =
      ItemFooterViewHolder(
        DataBindingUtil.inflate(
          LayoutInflater.from(parent.context),
          layoutId, parent, false
        ), retryCallback
      )
  }
}

