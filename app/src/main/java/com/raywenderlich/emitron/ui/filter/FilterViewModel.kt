package com.raywenderlich.emitron.ui.filter

import androidx.lifecycle.*
import com.raywenderlich.emitron.data.filter.FilterRepository
import com.raywenderlich.emitron.model.Attributes
import com.raywenderlich.emitron.model.ContentType
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.model.Difficulty
import com.raywenderlich.emitron.utils.Event
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * View model for filter view
 */
class FilterViewModel @Inject constructor(private val repository: FilterRepository) : ViewModel() {

  private val _domains = repository.getDomains()
  private val _loadFilterOptionsResult = MutableLiveData<Event<LoadFilterOptionResult>>()
  private val _categories = repository.getCategories()

  /**
   * Observer for domains
   */
  val domains: LiveData<List<Data>?> = Transformations.map(
      _domains
  ) {
    it?.map { domain ->
      Data.fromDomain(domain)
    }?.filter { data ->
      // Filter out archived domains
      !data.isLevelArchived()
    }
  }

  /**
   * Observer for categories
   */
  val categories: LiveData<List<Data>?> = Transformations.map(
      _categories
  ) {
    it?.map { category ->
      Data.fromCategory(category)
    }
  }


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
    FailedToFetchCategories
  }

  /**
   * Fetch domains from API
   *
   * This request will update our local db
   */
  fun getDomains() {

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
  fun getContentTypeList(contentTypeArray: Array<String>): List<Data> {
    return contentTypeArray.map { Data(attributes = Attributes(name = it)) }
  }

  /**
   * Transform difficulty array [Array<String>] to [List<Data>]
   *
   * @param difficultyArray Content type as [Array<String>]
   *
   * @return list of difficulty [Difficulty] as [List<Data>]
   */
  fun getDifficultyList(difficultyArray: Array<String>): List<Data> {
    return difficultyArray.map { Data(attributes = Attributes(name = it)) }
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
