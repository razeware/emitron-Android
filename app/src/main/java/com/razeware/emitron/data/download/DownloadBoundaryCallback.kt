package com.razeware.emitron.data.download

import androidx.paging.PagedList
import com.razeware.emitron.model.Data
import com.razeware.emitron.utils.NetworkState
import com.razeware.emitron.utils.PagedBoundaryCallback
import com.razeware.emitron.utils.PagedBoundaryCallbackImpl
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
    updateNetworkState(NetworkState.INIT_EMPTY)
    return
  }

  /**
   * See [PagedList.BoundaryCallback.onItemAtEndLoaded]
   */
  override fun onItemAtEndLoaded(itemAtEnd: Data) {
    updateNetworkState(NetworkState.SUCCESS)
    return
  }
}
