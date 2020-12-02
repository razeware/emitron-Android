package com.razeware.emitron.ui.filter

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.razeware.emitron.data.filter.FilterRepository
import com.razeware.emitron.model.*
import com.razeware.emitron.utils.Event
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * ViewModel for filter view
 */
class FilterViewModel @ViewModelInject constructor(private val repository: FilterRepository) :
  ViewModel() {

  private val _loadFilterOptionsResult = MutableLiveData<Event<LoadFilterOptionResult>>()
  private val _domains = repository.getDomains()
  private val _categories = repository.getCategories()

  /**
   * Observer for domains
   */
  val domains: LiveData<List<Data>> = _domains

  /**
   * Observer for categories
   */
  val categories: LiveData<List<Data>> = _categories


  /**
   * Observer for API result on fetching domains/categories
   */
  val loadFilterOptionsResult: LiveData<Event<LoadFilterOptionResult>>
    get() = _loadFilterOptionsResult

  /**
   * API error on loading filter options i.e. domains/categories
   */
  enum class LoadFilterOptionResult {
    /**
     * Request to load domain failed
     */
    FailedToFetchDomains,

    /**
     * Request to load categories failed
     */
    FailedToFetchCategories,

    /**
     * Request in progress
     */
    FetchingFilterOption
  }

  /**
   * Fetch domains from API
   *
   * This request will update our local db
   */
  fun getDomains() {
    _loadFilterOptionsResult.value = Event(LoadFilterOptionResult.FetchingFilterOption)

    val onFailure = {
      _loadFilterOptionsResult.value = Event(LoadFilterOptionResult.FailedToFetchDomains)
    }

    viewModelScope.launch {
      try {
        repository.fetchDomains()
      } catch (exception: IOException) {
        onFailure()
      } catch (exception: HttpException) {
        onFailure()
      }
    }
  }

  /**
   * Fetch categories from API
   *
   *  This request will update our local db
   */
  fun getCategories() {
    _loadFilterOptionsResult.value = Event(LoadFilterOptionResult.FetchingFilterOption)

    val onFailure = {
      _loadFilterOptionsResult.value = Event(LoadFilterOptionResult.FailedToFetchCategories)
    }

    viewModelScope.launch {
      try {
        repository.fetchCategories()
      } catch (exception: IOException) {
        onFailure()
      } catch (exception: HttpException) {
        onFailure()
      }
    }
  }

  /**
   * Transform content type array [Array<String>] to [List<Data>]
   *
   * @param contentTypeArray Content type as [Array<String>]
   *
   * @return list of content types [ContentType] as [List<Data>]
   */
  fun getContentTypeList(contentTypeArray: Map<ContentType, String>): List<Data> {
    return contentTypeArray.map {
      Data(
        type = FilterType.ContentType.toRequestFormat(),
        attributes = Attributes(name = it.value, contentType = it.key.toRequestFormat())
      )
    }
  }

  /**
   * Transform difficulty array [Array<String>] to [List<Data>]
   *
   * @param difficultyArray Content type as [Array<String>]
   *
   * @return list of difficulty [Difficulty] as [List<Data>]
   */
  fun getDifficultyList(difficultyArray: Array<String>): List<Data> {
    return difficultyArray.map {
      Data(
        type = FilterType.Difficulty.toRequestFormat(),
        attributes = Attributes(name = it)
      )
    }
  }

  /**
   * Check if domains are available in database
   *
   * @return True if domain are loaded, else False
   */
  fun hasDomains(): Boolean = domains.value?.isNotEmpty() ?: false

  /**
   * Check if categories are available in database
   *
   * @return True if categories are loaded, else False
   */
  fun hasCategories(): Boolean = categories.value?.isNotEmpty() ?: false
}
