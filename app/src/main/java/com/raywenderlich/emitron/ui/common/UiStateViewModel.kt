package com.raywenderlich.emitron.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.raywenderlich.emitron.utils.NetworkState
import com.raywenderlich.emitron.utils.UiStateManager

/**
 * Interface to be implemented by ViewModel class if it makes network requests or updates UI
 */
interface UiStateViewModel {

  /**
   * LiveData for [UiStateManager.UiState]
   */
  val uiState: MutableLiveData<UiStateManager.UiState>

  /**
   * LiveData for [NetworkState]
   */
  val networkState: LiveData<NetworkState>
}
