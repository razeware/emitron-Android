package com.razeware.emitron.data.content

import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.razeware.emitron.model.Content
import com.razeware.emitron.model.Contents
import com.razeware.emitron.model.Data
import com.razeware.emitron.utils.PagedResponse
import com.razeware.emitron.utils.async.ThreadManager
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Repository for content operations
 */
class ContentRepository @Inject constructor(
  private val api: ContentApi,
  private val threadManager: ThreadManager,
  private val settingsPref: com.raywenderlich.android.preferences.GeneralSettingsPrefs,
  private val contentDataSourceLocal: ContentDataSourceLocal
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
      sourceFactory.toLiveData(
        config = pagedListConfig,
        fetchExecutor = threadManager.networkExecutor
      )

    val uiState = Transformations.switchMap(sourceFactory.sourceLiveData) { it.uiState }

    val contents = Transformations.switchMap(sourceFactory.sourceLiveData) {
      it.contents
    }

    return PagedResponse(
      initialData = contents,
      pagedList = livePagedList,
      uiState = uiState,
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
  @AnyThread
  @Throws(Exception::class)
  suspend fun getContent(id: String): Content? {
    return withContext(threadManager.io) {
      api.getContent(id)
    }
  }

  /**
   * Load content from database
   *
   * @param id content id
   *
   * @return Content with id, null if not found in db
   */
  @AnyThread
  suspend fun getContentFromDb(id: String): Content? {
    return withContext(threadManager.io) {
      contentDataSourceLocal.getContent(id)?.toContent()
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
