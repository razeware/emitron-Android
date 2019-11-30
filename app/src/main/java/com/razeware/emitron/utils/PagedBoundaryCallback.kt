package com.razeware.emitron.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList

/**
 * Interface to delegate common code in [PagedList.BoundaryCallback]
 */
interface PagedBoundaryCallback {

  /**
   * Boundary call type
   */
  enum class BoundaryCallbackType {
    /**
     * No content in database, start loading from first page
     */
    INIT,
    /**
     * Has some content in database
     */
    APPENDING
  }

  /**
   * Has running requests
   *
   * @return True if a callback request already exists
   */
  fun isRunning(): Boolean

  /**
   * Get current network state observer
   */
  fun networkState(): LiveData<NetworkState>

  /**
   * Update current page number
   */
  fun updatePageNumber(pageNumber: Int? = null)

  /**
   * Get current page number
   */
  fun pageNumber(): Int?

  /**
   * Update if request running status
   *
   * @param running True if BoundaryCallback is running, else False
   */
  fun updateRunning(running: Boolean = false)

  /**
   * Update network state
   *
   * @param networkState Updated [NetworkState]
   */
  fun updateNetworkState(networkState: NetworkState)

  /**
   * Update boundary callback type
   */
  fun updateCallbackType(callbackType: BoundaryCallbackType = BoundaryCallbackType.INIT)

  /**
   * Handle API response success
   */
  fun handleError()

  /**
   * Handle API response empty
   */
  fun handleEmpty()

  /**
   * Handle API response success
   */
  fun handleSuccess()
}

/**
 * Common implementation for [PagedBoundaryCallback]
 */
class PagedBoundaryCallbackImpl : PagedBoundaryCallback {


  private val _networkState = MutableLiveData<NetworkState>().apply {
    value = NetworkState.INIT_SUCCESS
  }

  private var requestInProgress = false

  private var pageNumber: Int? = 0

  private var boundaryCallbackType: PagedBoundaryCallback.BoundaryCallbackType =
    PagedBoundaryCallback.BoundaryCallbackType.INIT

  override fun handleError() {
    if (pageNumber == 0 &&
      boundaryCallbackType == PagedBoundaryCallback.BoundaryCallbackType.INIT
    ) {
      _networkState.postValue(NetworkState.INIT_FAILED)
    } else {
      _networkState.postValue(NetworkState.FAILED)
    }
  }

  override fun handleEmpty() {
    if (pageNumber == 0 &&
      boundaryCallbackType == PagedBoundaryCallback.BoundaryCallbackType.INIT
    ) {
      _networkState.postValue(NetworkState.INIT_EMPTY)
    } else {
      _networkState.postValue(NetworkState.SUCCESS)
    }
  }

  override fun handleSuccess() {
    if (pageNumber == 0) {
      _networkState.postValue(NetworkState.INIT_SUCCESS)
    } else {
      _networkState.postValue(NetworkState.SUCCESS)
    }
  }

  override fun updateRunning(running: Boolean) {
    requestInProgress = running
  }

  override fun isRunning(): Boolean = requestInProgress

  override fun pageNumber(): Int? = pageNumber

  override fun updateNetworkState(networkState: NetworkState) {
    _networkState.value = networkState
  }

  override fun updateCallbackType(callbackType: PagedBoundaryCallback.BoundaryCallbackType) {
    boundaryCallbackType = callbackType
  }

  override fun updatePageNumber(pageNumber: Int?) {
    this.pageNumber = pageNumber
  }

  override fun networkState(): LiveData<NetworkState> = _networkState
}

