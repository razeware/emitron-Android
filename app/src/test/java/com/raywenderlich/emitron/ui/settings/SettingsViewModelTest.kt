package com.raywenderlich.emitron.ui.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.raywenderlich.emitron.data.login.LoginRepository
import com.raywenderlich.emitron.data.settings.SettingsRepository
import com.raywenderlich.emitron.utils.isEqualTo
import com.raywenderlich.emitron.utils.observeForTestingResult
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

  private val loginRepository: LoginRepository = mock()

  private val settingsRepository: SettingsRepository = mock()

  private lateinit var viewModel: SettingsViewModel

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @Before
  fun setUp() {
    // Given
    whenever(settingsRepository.getNightMode()).doReturn(1)
    whenever(settingsRepository.isCrashReportingAllowed()).doReturn(true)

    viewModel = SettingsViewModel(loginRepository, settingsRepository)
  }

  @Test
  fun init() {
    // When
    val nightMode = viewModel.nightMode.observeForTestingResult()
    val crashReportingAllowed = viewModel.crashReportingAllowed.observeForTestingResult()

    // Then
    verify(settingsRepository).getNightMode()
    verify(settingsRepository).isCrashReportingAllowed()

    nightMode isEqualTo 1
    crashReportingAllowed isEqualTo true
  }

  @Test
  fun logout() {
    viewModel.logout()
    verify(loginRepository).deleteUser()
  }

  @Test
  fun updateCrashReportingAllowed() {
    viewModel.updateCrashReportingAllowed(true)
    verify(settingsRepository).updateCrashReportingAllowed(true)
  }

  @Test
  fun updateNightMode() {
    viewModel.updateNightMode(1)
    val nightMode = viewModel.nightMode.observeForTestingResult()

    verify(settingsRepository).updateNightMode(1)
    assertThat(nightMode).isEqualTo(1)
  }
}
