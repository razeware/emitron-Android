package com.raywenderlich.emitron.data.content

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.utils.async.ThreadManager
import com.raywenderlich.emitron.utils.NetworkState

/**
 * Factory for [ContentDataSourceRemote]
 */
class ContentDataSourceFactoryRemote(
  private val pageSize: Int,
  private val contentApi: ContentApi,
  private val threadManager: ThreadManager,
  private val filters: List<Data>
) : DataSource.Factory<Int, Data>() {

  /**
   * Live data to observe [NetworkState] and Initial meta data from paged data source
   */
  val sourceLiveData: MutableLiveData<ContentDataSourceRemote> = MutableLiveData()

  /**
   * Factory function for [ContentDataSourceRemote]
   */
  override fun create(): DataSource<Int, Data> {
    val source = ContentDataSourceRemote(
      pageSize, contentApi, threadManager, filters
    )
    sourceLiveData.postValue(source)
    return source
  }
}
