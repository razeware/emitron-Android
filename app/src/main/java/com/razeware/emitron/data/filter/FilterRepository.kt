package com.razeware.emitron.data.filter

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.razeware.emitron.model.Data
import com.razeware.emitron.utils.async.ThreadManager
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Repository for filter operations
 */
class FilterRepository @Inject constructor(
  private val filterApi: FilterApi,
  private val filterDataSourceLocal: FilterDataSourceLocal,
  private val threadManager: ThreadManager
) {

  /**
   * Get categories observer
   *
   * @return LiveData<List<Data>> live data observer for categories table
   */
  fun getCategories(): LiveData<List<Data>> =
    Transformations.map(filterDataSourceLocal.getCategories()) {
      it.map { category -> category.toData() }
    }

  /**
   * Fetch content categories from server and store it to database
   *
   */
  @WorkerThread
  @Throws(Exception::class)
  suspend fun fetchCategories() {
    withContext(threadManager.io) {
      val categories = filterApi.getCategories()
      filterDataSourceLocal.saveCategories(categories.datum)
    }
  }

  /**
   * Fetch content domains from server and store it to database
   */
  @WorkerThread
  @Throws(Exception::class)
  suspend fun fetchDomains() {
    withContext(threadManager.io) {
      val domains = filterApi.getDomains()
      filterDataSourceLocal.saveDomains(domains.datum)
    }
  }

  /**
   * Fetch content domains and categories from server and store it to database
   */
  @WorkerThread
  @Throws(Exception::class)
  suspend fun fetchDomainsAndCategories() {
    withContext(threadManager.io) {
      val categories = filterApi.getCategories()
      filterDataSourceLocal.saveCategories(categories.datum)
      val domains = filterApi.getDomains()
      filterDataSourceLocal.saveDomains(domains.datum)
    }
  }

  /**
   * Get domains observer
   *
   * @return LiveData<List<Data>> live data observer for domains table
   */
  fun getDomains(): LiveData<List<Data>> =
    Transformations.map(filterDataSourceLocal.getDomains()) {
      it.map { domain ->
        domain.toData()
      }.filter { data ->
        !data.isLevelArchived()
      }
    }
}
