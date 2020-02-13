package com.razeware.emitron.data.content

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.razeware.emitron.model.ContentType
import com.razeware.emitron.model.Contents
import com.razeware.emitron.model.Data
import com.razeware.emitron.utils.UiStateManager
import com.razeware.emitron.utils.async.ThreadManager
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
   * UiState state LiveData
   */
  val uiState: MutableLiveData<UiStateManager.UiState> = MutableLiveData()

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
      threadManager.networkExecutor.execute {
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
    uiState.postValue(UiStateManager.UiState.LOADING)
    val loadAfterError = {
      retry = { loadAfter(params, callback) }
      uiState.postValue(UiStateManager.UiState.ERROR)
    }

    val response = getContent(pageNumber = params.key)

    if (response == null || !response.isSuccessful) {
      loadAfterError()
      return
    }

    val contentBody = response.body()
    if (contentBody == null) {
      loadAfterError()
      return
    }
    val items = contentBody.getDatumWithRelationships()
    if (items.isNullOrEmpty()) {
      loadAfterError()
      return
    }

    retry = null
    uiState.postValue(UiStateManager.UiState.LOADED)
    callback.onResult(items, (contentBody.getNextPage()))
  }

  /**
   * Load first page
   */
  override fun loadInitial(
    params: LoadInitialParams<Int>,
    callback: LoadInitialCallback<Int, Data>
  ) {

    uiState.postValue(UiStateManager.UiState.INIT)

    val loadInitialError = {
      retry = {
        loadInitial(params, callback)
      }
      uiState.postValue(UiStateManager.UiState.INIT_FAILED)
    }

    val loadInitialEmpty = {
      retry = {
        loadInitial(params, callback)
      }
      uiState.postValue(UiStateManager.UiState.INIT_EMPTY)
    }

    val response = getContent(pageNumber = 1)

    if (response == null || !response.isSuccessful) {
      loadInitialError()
      return
    }

    val contentBody = response.body()
    if (contentBody == null) {
      loadInitialError()
      return
    }

    val items = contentBody.getDatumWithRelationships()
    if (items.isNullOrEmpty()) {
      loadInitialEmpty()
      return
    }

    retry = null
    uiState.postValue(UiStateManager.UiState.INIT_LOADED)
    this.contents.postValue(contentBody.copy(datum = emptyList()))
    callback.onResult(items, null, (contentBody.getNextPage()))
  }

  private fun getContent(pageNumber: Int): Response<Contents>? {
    val contentTypesFromFilter = Data.getContentTypes(filters)
    val contentTypes = if (contentTypesFromFilter.isEmpty()) {
      ContentType.getAllowedContentTypes().toList()
    } else {
      contentTypesFromFilter
    }
    return try {
      contentApi.getContents(
        pageNumber = pageNumber,
        pageSize = pageSize,
        contentType = contentTypes,
        domain = Data.getDomainIds(filters),
        category = Data.getCategoryIds(filters),
        search = Data.getSearchTerm(filters),
        sort = Data.getSortOrder(filters),
        difficulty = Data.getDifficulty(filters),
        professional = Data.getProfessional(filters)
      ).execute()
    } catch (exception: IOException) {
      null
    } catch (exception: RuntimeException) {
      null
    }
  }
}
