package com.raywenderlich.emitron.data.download

import androidx.paging.PagedList
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.utils.NetworkState
import com.raywenderlich.emitron.utils.PagedBoundaryCallback
import com.raywenderlich.emitron.utils.PagedBoundaryCallbackImpl
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
