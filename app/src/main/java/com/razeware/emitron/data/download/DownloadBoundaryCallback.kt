package com.razeware.emitron.data.download

import androidx.paging.PagedList
import com.razeware.emitron.model.Data
import com.razeware.emitron.utils.PagedBoundaryCallback
import com.razeware.emitron.utils.PagedBoundaryCallbackImpl
import com.razeware.emitron.utils.UiStateManager
import javax.inject.Inject

/**
 * Download boundary callback to help us manage the UI with network state.
 */
class DownloadBoundaryCallback @Inject constructor(
  pagedBoundaryCallback: PagedBoundaryCallbackImpl
) : PagedList.BoundaryCallback<Data>(), PagedBoundaryCallback by pagedBoundaryCallback {

  /**
   * See [PagedList.BoundaryCallback.onZeroItemsLoaded]
   */
  override fun onZeroItemsLoaded() {
    updateUiState(UiStateManager.UiState.INIT_EMPTY)
    return
  }

  /**
   * See [PagedList.BoundaryCallback.onItemAtEndLoaded]
   */
  override fun onItemAtEndLoaded(itemAtEnd: Data) {
    updateUiState(UiStateManager.UiState.LOADED)
    return
  }
}
