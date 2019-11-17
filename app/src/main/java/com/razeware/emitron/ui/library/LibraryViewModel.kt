package com.razeware.emitron.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.razeware.emitron.data.content.ContentRepository
import com.razeware.emitron.data.filter.FilterRepository
import com.razeware.emitron.model.Data
import com.razeware.emitron.ui.content.ContentPagedViewModel
import com.razeware.emitron.utils.Log
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * ViewModel for library view
 */
class LibraryViewModel @Inject constructor(
  private val repository: ContentRepository,
  private val contentPagedViewModel: ContentPagedViewModel,
  private val filterRepository: FilterRepository
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

  /**
   * Add sync for domains and categories
   */
  fun syncDomainsAndCategories() {
    viewModelScope.launch {
      try {
        filterRepository.fetchDomainsAndCategories()
      } catch (exception: IOException) {
        Log.exception(exception)
      } catch (exception: HttpException) {
        Log.exception(exception)
      }
    }
  }

  /**
   * @return ContentPagedViewModel
   */
  fun getPaginationViewModel(): ContentPagedViewModel = contentPagedViewModel
}
