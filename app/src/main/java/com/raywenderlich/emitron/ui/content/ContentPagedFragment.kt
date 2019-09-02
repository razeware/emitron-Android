package com.raywenderlich.emitron.ui.content

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.emitron.model.Contents
import com.raywenderlich.emitron.utils.UiStateManager


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
    onContentsChange: ((Contents) -> Unit)? = null
  ) {

    initStateObserver(owner, contentPagedViewModel.uiState)

    contentPagedViewModel.contentPagedList.observe(owner, Observer { pagedList ->
      pagedList?.let {
        contentAdapter.submitList(it)
      }
    })

    contentPagedViewModel.networkState.observe(owner, Observer {
      contentAdapter.updateNetworkState(it)
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
