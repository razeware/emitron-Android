package com.razeware.emitron.ui.common

import androidx.lifecycle.MutableLiveData
import com.razeware.emitron.utils.UiStateManager

/**
 * Interface to be implemented by ViewModel class if it makes network requests or updates UI
 */
interface UiStateViewModel {

  /**
   * LiveData for [UiStateManager.UiState]
   */
  val uiState: MutableLiveData<UiStateManager.UiState>
}
