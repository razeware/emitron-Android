package com.razeware.emitron

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import com.razeware.emitron.data.login.LoginRepository
import com.razeware.emitron.data.settings.SettingsRepository
import com.razeware.emitron.model.Attributes
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.FilterType
import com.razeware.emitron.model.toRequestFormat
import com.razeware.emitron.ui.download.DownloadActionDelegate
import com.razeware.emitron.utils.isEqualTo
import com.razeware.emitron.utils.observeForTestingResult
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

  private val loginRepository: LoginRepository = mock()
  private val settingsRepository: SettingsRepository = mock()
  private val downloadActionDelegate: DownloadActionDelegate = mock()

  private lateinit var viewModel: MainViewModel

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @Before
  fun setUp() {
    viewModel = MainViewModel(loginRepository, settingsRepository, downloadActionDelegate)
  }

  @Test
  fun isAllowed_hasSubscription() {
    // Is logged in and has subscription

    // Given
    whenever(loginRepository.isLoggedIn()).doReturn(true)
    whenever(loginRepository.hasPermissions()).doReturn(true)

    // When
    val result = viewModel.isAllowed()

    // Then
    assertThat(result).isTrue()
    verify(loginRepository).isLoggedIn()
    verify(loginRepository).hasPermissions()
  }

  @Test
  fun isAllowed_hasNoSubscription() {
    // Is logged in and has no subscription

    // Given
    whenever(loginRepository.isLoggedIn()).doReturn(true)
    whenever(loginRepository.hasPermissions()).doReturn(false)

    // When
    val result = viewModel.isAllowed()

    // Then
    assertThat(result).isFalse()
    verify(loginRepository).isLoggedIn()
    verify(loginRepository).hasPermissions()
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
      Data(type = FilterType.Search.toRequestFormat(), attributes = Attributes(name = "Emitron")),
      Data(type = FilterType.Sort.toRequestFormat(), attributes = Attributes(name = "Newest"))
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
    viewModel.sortOrder.observeForTestingResult() isEqualTo "Newest"
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

  @Test
  fun getNightModeSettings() {
    // Given
    whenever(settingsRepository.getNightMode()).doReturn(1)

    // When
    val expected = viewModel.getNightModeSettings()

    // Then
    expected isEqualTo 1
    verify(settingsRepository).getNightMode()
    verifyNoMoreInteractions(settingsRepository)
  }

  @Test
  fun isCrashReportingAllowed() {
    // Given
    whenever(settingsRepository.isCrashReportingAllowed()).doReturn(true)

    // When
    val expected = viewModel.isCrashReportingAllowed()

    // Then
    expected isEqualTo true
    verify(settingsRepository).isCrashReportingAllowed()
    verifyNoMoreInteractions(settingsRepository)
  }

  @Test
  fun updateIsPlaying() {
    // When
    viewModel.updateIsPlaying(true)

    // Then
    viewModel.isPlaying.observeForTestingResult() isEqualTo true
  }

  @Test
  fun isPlaying() {
    // When
    viewModel.updateIsPlaying(true)

    // Then
    viewModel.isPlaying() isEqualTo true
  }
}
