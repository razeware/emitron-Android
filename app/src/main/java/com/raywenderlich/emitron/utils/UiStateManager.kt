package com.raywenderlich.emitron.utils

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
     * UI has an error
     */
    ERROR,
    /**
     * UI is loading some content
     */
    LOADING, // Used to indicate network resource is loading
    /**
     * UI has loaded
     */
    LOADED,
    /**
     * UI has an error due to no response from API
     */
    ERROR_EMPTY,
    /**
     * UI has an error due to connectivity failure
     */
    ERROR_CONNECTION;

    /**
     * Check if UI has any error
     *
     * @return true if UI has error, else false
     */
    fun hasError(): Boolean {
      return this == ERROR || this == ERROR_CONNECTION || this == ERROR_EMPTY
    }

    /**
     * Check if UI state is empty
     *
     * @return true if UI is empty, else false
     */
    fun isEmpty(): Boolean {
      return this == ERROR_EMPTY
    }
  }

  /**
   * Convenience function to observe UIState liveData from ViewModel
   */
  fun initStateObserver(lifeCycle: LifecycleOwner, uiState: LiveData<UiState>) {
    uiState.observe(lifeCycle, Observer {
      when (it) {
        UiState.ERROR -> onError()
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
