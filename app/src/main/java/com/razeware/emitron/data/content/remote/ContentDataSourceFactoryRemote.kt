package com.razeware.emitron.data.content.remote

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.razeware.emitron.data.content.local.ContentDataSourceLocal
import com.razeware.emitron.model.Data
import com.razeware.emitron.utils.async.ThreadManager

/**
 * Factory for [ContentDataSourceRemote]
 */
class ContentDataSourceFactoryRemote(
  private val pageSize: Int,
  private val contentApi: ContentApi,
  private val threadManager: ThreadManager,
  private val filters: List<Data>,
  private val contentDataSourceLocal: ContentDataSourceLocal
) : DataSource.Factory<Int, Data>() {

  /**
   * Live data to observe [UiStateManager.UiState] and Initial meta data from paged data source
   */
  val sourceLiveData: MutableLiveData<ContentDataSourceRemote> = MutableLiveData()

  /**
   * Factory function for [ContentDataSourceRemote]
   */
  override fun create(): DataSource<Int, Data> {
    val source = ContentDataSourceRemote(
      pageSize, contentApi, contentDataSourceLocal, threadManager, filters
    )
    sourceLiveData.postValue(source)
    return source
  }
}
