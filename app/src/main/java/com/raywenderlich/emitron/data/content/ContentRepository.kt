package com.raywenderlich.emitron.data.content

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.raywenderlich.emitron.model.Content
import com.raywenderlich.emitron.model.Contents
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.ui.settings.SettingsPrefs
import com.raywenderlich.emitron.utils.PagedResponse
import com.raywenderlich.emitron.utils.async.ThreadManager
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Repository for content operations
 */
class ContentRepository @Inject constructor(
  private val api: ContentApi,
  private val threadManager: ThreadManager,
  private val settingsPref: SettingsPrefs
) {

  /**
   * Fetch contents
   *
   * @param filters List of filters. Filters can consist categories, domains or difficulty
   * @param pageSize Default page size to be fetched
   *
   * @return [PagedResponse] containing LiveData objects of network state,
   * initial meta data, retry callback and paged list
   */
  @MainThread
  fun getContents(
    filters: List<Data>,
    pageSize: Int = 10
  ): PagedResponse<Contents, Data> {

    val sourceFactory = ContentDataSourceFactoryRemote(
      pageSize,
      api,
      threadManager,
      filters
    )

    val pagedListConfig = PagedList.Config.Builder()
      .setInitialLoadSizeHint(pageSize)
      .setPageSize(pageSize)
      .build()

    val livePagedList =
      sourceFactory.toLiveData(config = pagedListConfig, fetchExecutor = threadManager.networkIo)

    val networkState = Transformations.switchMap(sourceFactory.sourceLiveData) { it.networkState }

    val contents = Transformations.switchMap(sourceFactory.sourceLiveData) {
      it.contents
    }

    return PagedResponse(
      initialData = contents,
      pagedList = livePagedList,
      networkState = networkState,
      retry =
      {
        sourceFactory.sourceLiveData.value?.retryAllFailed()
      }
    )
  }

  /**
   * Get content
   *
   * @param id Content id to be fetched
   *
   * @return [Content] Response Content
   */
  @WorkerThread
  @Throws(Exception::class)
  suspend fun getContent(id: String): Content {
    return withContext(threadManager.io) {
      api.getContent(id)
    }
  }

  /**
   * Get recently searched queries
   *
   * @return list of recent search queries
   */
  fun getSearchQueries(): List<String> = settingsPref.getSearchQueries()

  /**
   * Save recent query
   *
   * @param query search query
   */
  fun saveSearchQuery(query: String): Unit = settingsPref.saveSearchQuery(query)
}
