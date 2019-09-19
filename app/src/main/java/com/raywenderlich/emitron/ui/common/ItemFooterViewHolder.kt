package com.raywenderlich.emitron.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.emitron.databinding.ItemFooterBinding
import com.raywenderlich.emitron.utils.NetworkState
import com.raywenderlich.emitron.utils.extensions.toVisibility

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
   * @param networkState Network status
   */
  fun bindTo(networkState: NetworkState?) {
    viewDataBinding.progressBar.toVisibility(networkState == NetworkState.RUNNING)
    viewDataBinding.textViewProgress.toVisibility(networkState == NetworkState.RUNNING)
    viewDataBinding.buttonRetry.toVisibility(networkState == NetworkState.FAILED)
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

