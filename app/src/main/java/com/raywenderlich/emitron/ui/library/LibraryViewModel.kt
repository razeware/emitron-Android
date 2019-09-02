package com.raywenderlich.emitron.ui.library

import androidx.lifecycle.ViewModel
import com.raywenderlich.emitron.data.content.ContentRepository
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.ui.content.ContentPagedViewModel
import javax.inject.Inject

/**
 * View model for library view
 */
class LibraryViewModel @Inject constructor(
    private val repository: ContentRepository,
    /**
     * Common view model to handle pagination related code
     */
    val contentPagedViewModel: ContentPagedViewModel
) : ViewModel() {

  /**
   * Load collections
   *
   * @param filters List of filters
   */
  fun loadCollections(filters: List<Data> = emptyList()) {
    val listing = repository.getContents(filters = filters)
    contentPagedViewModel.repoResult.postValue(listing)
  }

  /**
   * Load recently searched queries
   *
   * @return list of recent search queries
   */
  fun loadSearchQueries(): List<String> = repository.getSearchQueries()

  /**
   * Save recent query
   *
   * @param query search query
   */
  fun saveSearchQuery(query: String): Unit = repository.saveSearchQuery(query)
}
