package com.razeware.emitron.utils

import androidx.lifecycle.LiveData
import androidx.paging.PagedList

/**
 * Data transfer object for paged list
 */
data class PagedResponse<C, T>(
  /** meta data common to all list items */
  val initialData: LiveData<C>? = null,
  /** paged list */
  val pagedList: LiveData<PagedList<T>>,
  /** represents the network request status*/
  val uiState: LiveData<UiStateManager.UiState>? = null,
  /** represents the refresh status*/
  val retry: (() -> Unit)? = null
)

/**
 * Data transfer object for local database backed paged list
 */
data class LocalPagedResponse<T>(
  /** paged list */
  val pagedList: LiveData<PagedList<T>>,
  /** represents the network request status of boundary callback*/
  val uiState: LiveData<UiStateManager.UiState>? = null
)
