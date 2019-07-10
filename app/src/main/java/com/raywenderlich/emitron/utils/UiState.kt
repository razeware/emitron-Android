package com.raywenderlich.emitron.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

enum class UiState {
  ERROR,
  LOADING, // Used to indicate network resource is loading
  LOADED,
  ERROR_CONNECTION;

  fun hasError(): Boolean {
    return this == ERROR || this == ERROR_CONNECTION
  }
}


interface UiStateManager {

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

  val onError: () -> Unit
  val onErrorConnection: () -> Unit
  val onLoading: () -> Unit
  val onLoaded: () -> Unit
}
