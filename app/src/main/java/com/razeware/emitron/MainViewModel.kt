package com.razeware.emitron

import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.razeware.emitron.data.login.LoginRepository
import com.razeware.emitron.data.settings.SettingsRepository
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.SortOrder
import com.razeware.emitron.ui.download.DownloadAction
import com.razeware.emitron.ui.download.DownloadActionDelegate

/**
 * Parent Viewmodel for all fragment
 *
 * @param loginRepository [LoginRepository] to verify/get user login details
 */
class MainViewModel @ViewModelInject constructor(
  private val loginRepository: LoginRepository,
  private val settingsRepository: SettingsRepository,
  private val downloadActionDelegate: DownloadActionDelegate
) : ViewModel(), DownloadAction by downloadActionDelegate {

  /**
   * @return True if user is logged in, otherwise False
   */
  private fun isLoggedIn(): Boolean = loginRepository.isLoggedIn()

  /**
   * @return True if user has subscription, otherwise False
   */
  private fun hasPermissions(): Boolean = loginRepository.hasPermissions()

  /**
   * @return True if user is allowed to use app, otherwise False
   */
  fun isAllowed(): Boolean = isLoggedIn() && hasPermissions()

  private val _selectedFilters = MutableLiveData<List<Data>>()

  private val _query = MutableLiveData<String>()

  private val _sortOrder = MutableLiveData<String>()

  /**
   * Observer for selected filters
   */
  val selectedFilters: LiveData<List<Data>>
    get() = _selectedFilters

  /**
   * Observer for current query
   */
  val query: LiveData<String>
    get() = _query

  /**
   * Observer for sort order
   */
  val sortOrder: LiveData<String>
    get() = _sortOrder


  private val _isPlaying = MutableLiveData<Boolean>()

  /**
   * Observer for playback state
   */
  val isPlaying: LiveData<Boolean>
    get() = _isPlaying

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
    _sortOrder.value = when (SortOrder.fromValue(sortOrder)) {
      SortOrder.Newest -> SortOrder.Popularity
      SortOrder.Oldest -> SortOrder.Popularity
      SortOrder.Popularity -> SortOrder.Newest
      else -> SortOrder.Newest
    }.name
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

  /**
   * Get selected night mode from preference
   *
   * @return Either of [AppCompatDelegate.MODE_NIGHT_YES],
   * [AppCompatDelegate.MODE_NIGHT_NO], [AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM]
   */
  fun getNightModeSettings(): Int = settingsRepository.getNightMode()

  /**
   * Get if user has allowed crash reporting
   *
   * @return True if user has allowed, else False
   */
  fun isCrashReportingAllowed(): Boolean = settingsRepository.isCrashReportingAllowed()

  /**
   * Check if a content is being played
   *
   * @return true if playing a content, else false
   */
  fun isPlaying(): Boolean = _isPlaying.value ?: false

  /**
   * Update playback status
   *
   * @param isPlaying true if playing a content, else false
   */
  fun updateIsPlaying(isPlaying: Boolean = false) {
    _isPlaying.value = isPlaying
  }
}
