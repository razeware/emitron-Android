package com.raywenderlich.emitron.data.filter

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.raywenderlich.emitron.model.Category
import com.raywenderlich.emitron.model.Domain
import com.raywenderlich.emitron.utils.async.ThreadManager
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Repository for filter operations
 */
class FilterRepository @Inject constructor(
  private val filterDataSourceLocal: FilterDataSourceLocal,
  private val filterApi: FilterApi,
  private val threadManager: ThreadManager
) {

  /**
   * Get categories observer
   *
   * @return LiveData<List<Category>> live data observer for categories table
   */
  fun getCategories(): LiveData<List<Category>> = filterDataSourceLocal.getCategories()

  /**
   * Fetch content categories from server and store it to db
   *
   */
  @WorkerThread
  suspend fun fetchCategories() {
    withContext(threadManager.io) {
      val categories = filterApi.getCategories()
      filterDataSourceLocal.saveCategories(categories.datum)
    }
  }

  /**
   * Fetch content domains from server and store it to db
   *
   */
  @WorkerThread
  suspend fun fetchDomains() {
    withContext(threadManager.io) {
      val domains = filterApi.getDomains()
      filterDataSourceLocal.saveDomains(domains.datum)
    }
  }

  /**
   * Get domains observer
   *
   * @return LiveData<List<Category>> live data observer for domains table
   */
  fun getDomains(): LiveData<List<Domain>> = filterDataSourceLocal.getDomains()
}
