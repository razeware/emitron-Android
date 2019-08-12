package com.raywenderlich.emitron.ui.content

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.switchMap
import androidx.paging.PagedList
import com.raywenderlich.emitron.model.Contents
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.ui.common.UiStateViewModel
import com.raywenderlich.emitron.utils.NetworkState
import com.raywenderlich.emitron.utils.PagedResponse
import com.raywenderlich.emitron.utils.UiStateManager
import javax.inject.Inject

/**
 * Delegate view model class for common code related to paging library (pagination) integration
 */
class ContentPagedViewModel @Inject constructor() : UiStateViewModel {

  /**
   * Live data for [PagedResponse]
   */
  val repoResult: MutableLiveData<PagedResponse<Contents, Data>> = MutableLiveData()

  /**
   * Live data for [UiStateManager.UiState]
   */
  override val uiState: MutableLiveData<UiStateManager.UiState> = MutableLiveData()

  /**
   * Live data for [NetworkState]
   */
  override val networkState: LiveData<NetworkState> = switchMap(repoResult) { it.networkState }

  /**
   * Live data for [Contents] meta data for list items
   */
  val contents: LiveData<Contents> = switchMap(repoResult) { it.initialData }

  /**
   * Live data for [Data] paged list
   */
  val contentPagedList: LiveData<PagedList<Data>> = switchMap(repoResult) { it.pagedList }

  /**
   * Function to handle item retry on error
   */
  fun handleItemRetry(isNetConnected: Boolean) {
    if (!isNetConnected && contentPagedList.value == null) {
      uiState.postValue(UiStateManager.UiState.ERROR_CONNECTION)
      return
    }

    repoResult.value?.retry?.invoke()
  }
}
