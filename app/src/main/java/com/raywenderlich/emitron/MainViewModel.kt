package com.raywenderlich.emitron

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.raywenderlich.emitron.data.login.LoginRepository
import com.raywenderlich.emitron.model.Data
import javax.inject.Inject

/**
 * Parent Viewmodel for all fragment
 *
 * @param loginRepository [LoginRepository] to verify/get user login details
 */
class MainViewModel @Inject constructor(private val loginRepository: LoginRepository) :
  ViewModel() {

  /**
   * @return True if user is logged in, otherwise False
   */
  private fun isLoggedIn(): Boolean = loginRepository.isLoggedIn()

  /**
   * @return True if user has subscription, otherwise False
   */
  private fun hasSubscription(): Boolean = loginRepository.hasSubscription()

  /**
   * @return True if user is allowed to use app, otherwise False
   */
  fun isAllowed(): Boolean = isLoggedIn() && hasSubscription()

  private val _selectedFilters = MutableLiveData<List<Data>>()

  private val _query = MutableLiveData<String>()

  private val _sortOrder = MutableLiveData<String>()


  /**
   * Observer for selected filters
   */
  val selectedFilters: LiveData<List<Data>>
    get() = _selectedFilters

  /**
   * Observer for selected filters
   */
  val query: LiveData<String>
    get() = _query

  /**
   * Observer for selected filters
   */
  val sortOrder: LiveData<String>
    get() = _sortOrder

  /**
   * Set selected filter
   *
   * @param filters List<Data> list of filter
   */
  fun setSelectedFilters(filters: List<Data>) {
    _selectedFilters.value = filters
  }

  /**
   * Get selected filter
   *
   * @return List<Data> Selected filters
   */
  fun getSelectedFilters(withSearch: Boolean = false, withSort: Boolean = false): List<Data> {
    val filters = selectedFilters.value ?: emptyList()

    val selectedQuery = query.value
    val searchFilter = if (withSearch && !selectedQuery.isNullOrBlank()) {
      Data.fromSearchQuery(selectedQuery)
    } else {
      null
    }
    val sortOrder = sortOrder.value
    val sortFilter = if (withSort && !sortOrder.isNullOrBlank()) {
      Data.fromSortOrder(sortOrder)
    } else {
      null
    }
    return filters.plus(searchFilter).plus(sortFilter).filterNotNull()
  }

  /**
   * Check if any filters are applied
   *
   * @return True if filters are applied, else False
   */
  fun hasFilters(): Boolean = getSelectedFilters().isNotEmpty()

  /**
   * Remove on filter
   *
   * @param filter [Data] filter to be removed
   */
  fun removeFilter(filter: Data?) {
    if (null == filter) return
    _selectedFilters.value = _selectedFilters.value?.minus(filter)
  }

  /**
   * Clear all filters
   */
  fun resetFilters() {
    _selectedFilters.value = emptyList()
  }

  /**
   * Add search filter
   *
   * @param searchTerm Search keyword
   */
  fun setSearchQuery(searchTerm: String?) {
    _query.value = searchTerm
  }


  /**
   * Add sort order
   *
   * @param sortOrder Sort order
   */
  fun setSortOrder(sortOrder: String?) {
    _sortOrder.value = sortOrder?.toLowerCase()
  }

  /**
   * Clear search query
   *
   * @return True if search query was cleared else False
   */
  fun clearSearchQuery(): Boolean = if (_query.value.isNullOrBlank()) {
    false
  } else {
    _query.value = ""
    true
  }
}
