package com.raywenderlich.emitron

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.raywenderlich.emitron.data.login.LoginRepository
import com.raywenderlich.emitron.model.Attributes
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.model.DataType
import com.raywenderlich.emitron.utils.isEqualTo
import com.raywenderlich.emitron.utils.observeForTestingResult
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

  private val loginRepository: LoginRepository = mock()

  private lateinit var viewModel: MainViewModel

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @Before
  fun setUp() {
    viewModel = MainViewModel(loginRepository)
  }

  @Test
  fun isAllowed_hasSubscription() {
    // Is logged in and has subscription

    // Given
    whenever(loginRepository.isLoggedIn()).doReturn(true)
    whenever(loginRepository.hasSubscription()).doReturn(true)

    // When
    val result = viewModel.isAllowed()

    // Then
    assertThat(result).isTrue()
    verify(loginRepository).isLoggedIn()
    verify(loginRepository).hasSubscription()
  }

  @Test
  fun isAllowed_hasNoSubscription() {
    // Is logged in and has no subscription

    // Given
    whenever(loginRepository.isLoggedIn()).doReturn(true)
    whenever(loginRepository.hasSubscription()).doReturn(false)

    // When
    val result = viewModel.isAllowed()

    // Then
    assertThat(result).isFalse()
    verify(loginRepository).isLoggedIn()
    verify(loginRepository).hasSubscription()
  }

  @Test
  fun setSelectedFilters() {
    // Given
    val filterList = listOf(Data("1"), Data("2"))

    // When
    viewModel.setSelectedFilters(filterList)

    // Then
    viewModel.selectedFilters.observeForTestingResult() isEqualTo filterList
    viewModel.getSelectedFilters() isEqualTo filterList
  }

  @Test
  fun getSelectedFilters() {
    // Given
    val filterList = listOf(
      Data("1"),
      Data("2")
    )
    viewModel.setSearchQuery("Emitron")
    viewModel.setSelectedFilters(filterList)

    // Then
    viewModel.getSelectedFilters() isEqualTo listOf(
      Data("1"),
      Data("2")
    )
  }

  @Test
  fun getSelectedFilters_withSearchAndSort() {
    // Given
    val filterList = listOf(
      Data("1"),
      Data("2")
    )
    viewModel.setSearchQuery("Emitron")
    viewModel.setSortOrder("Popularity")

    // When
    viewModel.setSelectedFilters(filterList)

    // Then
    val expectedList = listOf(
      Data("1"),
      Data("2"),
      Data(type = DataType.Search.toRequestFormat(), attributes = Attributes(name = "Emitron")),
      Data(type = DataType.Sort.toRequestFormat(), attributes = Attributes(name = "popularity"))
    )
    viewModel.getSelectedFilters(true, withSort = true) isEqualTo expectedList
  }

  @Test
  fun hasFilters() {
    // Given
    val filterList = listOf(
      Data("1"),
      Data("2")
    )

    // When
    viewModel.setSelectedFilters(filterList)

    // Then
    viewModel.hasFilters() isEqualTo true


    // When
    viewModel.resetFilters()
    viewModel.setSearchQuery("Emitron")
    viewModel.setSortOrder("popularity")

    // Then
    viewModel.hasFilters() isEqualTo false
  }

  @Test
  fun removeFilter() {
    // Given
    val filterList = listOf(
      Data("1"),
      Data("2")
    )
    viewModel.setSelectedFilters(filterList)

    // When
    viewModel.removeFilter(Data("1"))

    // Then
    viewModel.selectedFilters.observeForTestingResult() isEqualTo listOf(Data("2"))
    viewModel.getSelectedFilters() isEqualTo listOf(Data("2"))
  }

  @Test
  fun resetFilters() {
    // Given
    val filterList = listOf(
      Data("1"),
      Data("2")
    )
    viewModel.setSelectedFilters(filterList)

    // When
    viewModel.resetFilters()

    // Then
    viewModel.selectedFilters.observeForTestingResult() isEqualTo emptyList<Data>()
    viewModel.getSelectedFilters() isEqualTo emptyList<Data>()
  }

  @Test
  fun setSearchQuery() {
    // When
    viewModel.setSearchQuery("Emitron")

    // Then
    viewModel.query.observeForTestingResult() isEqualTo "Emitron"
  }

  @Test
  fun setSortOrder() {
    // When
    viewModel.setSortOrder("Popularity")

    // Then
    viewModel.sortOrder.observeForTestingResult() isEqualTo "popularity"

  }

  @Test
  fun clearSearchQuery() {
    // Given
    viewModel.setSearchQuery("Emitron")

    // When
    val result = viewModel.clearSearchQuery()

    // Then
    viewModel.query.observeForTestingResult() isEqualTo ""
    result isEqualTo true

    // When
    val result2 = viewModel.clearSearchQuery()

    // Then
    result2 isEqualTo false
  }
}
