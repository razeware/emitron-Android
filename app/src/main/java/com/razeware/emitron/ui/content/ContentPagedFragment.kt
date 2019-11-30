package com.razeware.emitron.ui.content

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.razeware.emitron.model.Contents
import com.razeware.emitron.utils.NetworkState
import com.razeware.emitron.utils.UiStateManager


/**
 * Delegate view class for common code related to paging library (pagination) integration
 */
class ContentPagedFragment(
  private val contentPagedViewModel: ContentPagedViewModel,
  private val contentAdapter: ContentAdapter
) : UiStateManager {

  override val onError: () -> Unit = {
    // notify adapter to show error state
    contentAdapter.updateErrorState(UiStateManager.UiState.ERROR)
  }

  override val onErrorEmpty: () -> Unit = {
    // notify adapter to show error state
    contentAdapter.updateErrorState(UiStateManager.UiState.ERROR_EMPTY)
  }

  override val onErrorConnection: () -> Unit = {
    contentAdapter.updateErrorState(UiStateManager.UiState.ERROR_CONNECTION)
  }

  override val onLoading: () -> Unit = {
    contentAdapter.updateUiState(UiStateManager.UiState.LOADING)
  }

  override val onLoaded: () -> Unit = {
    contentAdapter.updateUiState(UiStateManager.UiState.LOADED)
  }

  /**
   * Initialise common code related to paging library
   */
  fun initPaging(
    owner: LifecycleOwner,
    recyclerView: RecyclerView,
    onNetworkStateChange: ((NetworkState) -> Unit)? = null,
    onContentsChange: ((Contents) -> Unit)? = null
  ) {

    initStateObserver(owner, contentPagedViewModel.uiState)

    contentPagedViewModel.networkState.observe(owner, Observer {
      contentAdapter.updateNetworkState(it)
      onNetworkStateChange?.invoke(it)
    })

    contentPagedViewModel.contentPagedList.observe(owner, Observer { pagedList ->
      pagedList?.let {
        contentAdapter.submitList(it)
      }
    })

    contentPagedViewModel.contents.observe(owner, Observer {
      contentAdapter.included = it.included
      onContentsChange?.invoke(it)
    })

    with(recyclerView) {
      layoutManager = LinearLayoutManager(context)
      setHasFixedSize(true)
      adapter = contentAdapter
    }
  }

}
