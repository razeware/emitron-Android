package com.raywenderlich.emitron.data.content

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.raywenderlich.emitron.model.ContentType
import com.raywenderlich.emitron.model.Contents
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.utils.NetworkState
import com.raywenderlich.emitron.utils.async.ThreadManager
import retrofit2.Response
import java.io.IOException

/**
 * PagedKeyedDataSource for [Contents]
 */
class ContentDataSourceRemote(
  private val pageSize: Int,
  private val contentApi: ContentApi,
  private val threadManager: ThreadManager,
  private val filters: List<Data>
) : PageKeyedDataSource<Int, Data>() {

  private var retry: (() -> Any)? = null

  /**
   * Network state LiveData
   */
  val networkState: MutableLiveData<NetworkState> = MutableLiveData()

  /**
   * Meta data LiveData
   * You will use this to pass initial included data to the adapter
   */
  val contents: MutableLiveData<Contents> = MutableLiveData()

  /**
   * Retry all failed requests
   */
  fun retryAllFailed() {
    val prevRetry = retry
    retry = null
    prevRetry?.let {
      threadManager.networkIo.execute {
        it.invoke()
      }
    }
  }

  /**
   * Load previous page
   */
  override fun loadBefore(
    params: LoadParams<Int>,
    callback: LoadCallback<Int, Data>
  ) {
    // ignored, since you only ever append to you initial load
  }

  /**
   * Load next page
   */
  override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Data>) {
    networkState.postValue(NetworkState.LOADING)
    val loadAfterError = {
      retry = { loadAfter(params, callback) }
      networkState.postValue(NetworkState.ERROR)
    }

    val response = try {
      getContent(pageNumber = params.key)
    } catch (exception: IOException) {
      null
    }

    if (response == null || !response.isSuccessful) {
      loadAfterError()
      return
    }

    val contentBody = response.body()
    if (contentBody == null) {
      loadAfterError()
      return
    }
    val items = contentBody.datum
    if (items.isNullOrEmpty()) {
      loadAfterError()
      return
    }

    retry = null
    networkState.postValue(NetworkState.LOADED)
    callback.onResult(items, (contentBody.getNextPage()))
  }

  /**
   * Load first page
   */
  override fun loadInitial(
    params: LoadInitialParams<Int>,
    callback: LoadInitialCallback<Int, Data>
  ) {

    networkState.postValue(NetworkState.INIT)

    val loadInitialError = {
      retry = {
        loadInitial(params, callback)
      }
      networkState.postValue(NetworkState.INIT_ERROR)
    }

    val loadInitialEmpty = {
      retry = {
        loadInitial(params, callback)
      }
      networkState.postValue(NetworkState.INIT_EMPTY)
    }

    val response = try {
      getContent(pageNumber = 1)
    } catch (exception: IOException) {
      null
    }

    if (response == null || !response.isSuccessful) {
      loadInitialError()
      return
    }

    val contentBody = response.body()
    if (contentBody == null) {
      loadInitialError()
      return
    }

    val items = contentBody.datum
    if (items.isNullOrEmpty()) {
      loadInitialEmpty()
      return
    }

    retry = null
    networkState.postValue(NetworkState.LOADED)
    this.contents.postValue(contentBody.copy(datum = emptyList()))
    callback.onResult(items, null, (contentBody.getNextPage()))
  }

  private fun getContent(pageNumber: Int): Response<Contents>? = contentApi.getContents(
    pageNumber = pageNumber,
    pageSize = pageSize,
    contentType = ContentType.getAllowedContentType().toList(),
    domain = Data.getDomainIds(filters),
    category = Data.getCategoryIds(filters)
  ).execute()
}
