package com.razeware.emitron.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/**
 * Helper interface to represent UI state.
 *
 * You should implement the interface to simplify UI state
 */
interface UiStateManager {

  /**
   * Enum class to current UI state
   */
  enum class UiState {
    /**
     * Request successful, but no data
     */
    EMPTY,
    /**
     * Initial request in progress
     */
    INIT,
    /**
     * Initial request failed
     */
    INIT_FAILED,
    /**
     * Initial request successful, but no data
     */
    INIT_EMPTY,
    /**
     * Initial request successful
     */
    INIT_LOADED,
    /**
     * UI has an error
     */
    ERROR,
    /**
     * UI is loading some content
     */
    LOADING,
    /**
     * UI has loaded
     */
    LOADED,
    /**
     * UI has an error due to connectivity failure
     */
    ERROR_CONNECTION;

    /**
     * Check if UI state is empty
     *
     * @return true if UI is empty, else false
     */
    fun isEmpty(): Boolean {
      return this == EMPTY || this == INIT_EMPTY
    }
  }

  /**
   * Convenience function to observe UIState liveData from ViewModel
   */
  fun initStateObserver(lifeCycle: LifecycleOwner, uiState: LiveData<UiState>) {
    uiState.observe(lifeCycle, Observer {
      when (it) {
        UiState.ERROR -> onError()
        UiState.EMPTY -> onErrorEmpty()
        UiState.LOADING -> onLoading()
        UiState.LOADED -> onLoaded()
        UiState.ERROR_CONNECTION -> onErrorConnection()
        else -> {// Ignoring this
        }
      }
    })
  }

  /**
   * Handle UI on any error
   */
  val onError: () -> Unit

  /**
   * Handle UI on any error
   */
  val onErrorEmpty: () -> Unit
  /**
   * Handle UI on connectivity failure
   */
  val onErrorConnection: () -> Unit
  /**
   * Handle UI on any request progress
   */
  val onLoading: () -> Unit
  /**
   * Handle UI after any request success/failure
   */
  val onLoaded: () -> Unit
}

/**
 * Is loading.
 */
fun UiStateManager.UiState?.isLoading(): Boolean {
  return this != null && (
      this == UiStateManager.UiState.LOADING || this == UiStateManager.UiState.INIT)
}

/**
 * Has error.
 */
fun UiStateManager.UiState?.hasFailed(): Boolean {
  return this != null && (this == UiStateManager.UiState.ERROR)
}

/**
 * Has error, connection error, or no data.
 */
fun UiStateManager.UiState?.hasError(): Boolean {
  return this != null &&
      (
          this == UiStateManager.UiState.ERROR ||
              this == UiStateManager.UiState.INIT_FAILED ||
              this == UiStateManager.UiState.ERROR_CONNECTION ||
              this == UiStateManager.UiState.EMPTY ||
              this == UiStateManager.UiState.INIT_EMPTY
          )
}
