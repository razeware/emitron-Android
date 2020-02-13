package com.razeware.emitron.ui.common

import com.razeware.emitron.utils.UiStateManager
import com.razeware.emitron.utils.hasError
import com.razeware.emitron.utils.isLoading

/**
 * Delegate adapter class for common code related to updating UI on [UiStateManager.UiState] changes
 */
class PagedAdapter {

  /**
   * Current UI state
   */
  var uiState: UiStateManager.UiState? = null

  /**
   * Current error state
   */
  private var errorState: UiStateManager.UiState? = null

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
   * Update UI [UiStateManager.UiState] for error
   */
  fun updateErrorState(
    itemCount: Int,
    newErrorState: UiStateManager.UiState?,
    onChangeItem: (Int) -> Unit,
    onChangeLastItem: (Boolean) -> Unit
  ) {
    if (errorState != newErrorState) {
      errorState = newErrorState
      updateUiState(itemCount, newErrorState, onChangeItem, onChangeLastItem)
    }
  }

  /**
   * Check if UI has error
   */
  fun hasUiStateError(): Boolean = uiState.hasError()

  /**
   * Check if footer progress item view should be shown
   */
  fun hasExtraRow(): Boolean = uiState.hasError() || uiState.isLoading()
}
