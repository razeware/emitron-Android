package com.raywenderlich.emitron.utils

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
  val networkState: LiveData<NetworkState>? = null,
  /** represents the refresh status*/
  val retry: (() -> Unit)? = null
)
