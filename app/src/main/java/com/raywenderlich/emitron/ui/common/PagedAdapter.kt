package com.raywenderlich.emitron.ui.common

import com.raywenderlich.emitron.utils.NetworkState
import com.raywenderlich.emitron.utils.UiStateManager

/**
 * Delegate adapter class for common code related to updating UI on [UiStateManager.UiState] or
 * [NetworkState] changes
 */
class PagedAdapter {

  /**
   * Current network state
   */
  var networkState: NetworkState? = null

  /**
   * Current UI state
   */
  var uiState: UiStateManager.UiState? = null

  /**
   * Current error state
   */
  var errorState: UiStateManager.UiState? = null

  /**
   * Update UI on [NetworkState] change
   */
  fun updateNetworkState(
    itemCount: Int,
    newNetworkState: NetworkState?,
    onChangeItem: (Int) -> Unit,
    onChangeLastItem: (Boolean) -> Unit
  ) {
    if (newNetworkState == NetworkState.INIT_FAILED) {
      updateUiState(itemCount, UiStateManager.UiState.ERROR, onChangeItem, onChangeLastItem)
      return
    }

    if (newNetworkState == NetworkState.INIT_EMPTY) {
      updateUiState(itemCount, UiStateManager.UiState.ERROR_EMPTY, onChangeItem, onChangeLastItem)
      return
    }

    val previousState = this.networkState
    val hadExtraRow = hasExtraRow()
    this.networkState = newNetworkState

    if (newNetworkState == NetworkState.INIT ||
      newNetworkState == NetworkState.RUNNING
    ) {
      uiState = null
    }
    val hasExtraRow = hasExtraRow()
    if (hadExtraRow != hasExtraRow) {
      onChangeLastItem(hadExtraRow)
    } else if (hasExtraRow && previousState != newNetworkState) {
      onChangeItem(itemCount - 1)
    }
  }

  /**
   * Update UI on [UiStateManager.UiState] change
   */
  fun updateUiState(
    itemCount: Int,
    newUiState: UiStateManager.UiState?,
    onChangeItem: (Int) -> Unit,
    onChangeLastItem: (Boolean) -> Unit
  ) {
    val previousState = this.uiState
    val hadExtraRow = hasExtraRow()
    this.uiState = newUiState
    val hasExtraRow = hasExtraRow()
    if (hadExtraRow != hasExtraRow) {
      onChangeLastItem(hadExtraRow)
    } else if (hasExtraRow && previousState != newUiState) {
      onChangeItem(itemCount - 1)
    }
  }

  /**
   * Check if UI has error
   */
  fun hasUiStateError(): Boolean = uiState?.hasError() ?: false

  /**
   * Check if footer progress item view should be shown
   */
  fun hasExtraRow(): Boolean = (null != networkState && networkState != NetworkState.SUCCESS
      && networkState != NetworkState.INIT_SUCCESS) ||
      (null != uiState && uiState != UiStateManager.UiState.LOADED)
}
