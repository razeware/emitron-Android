package com.raywenderlich.emitron.data.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.raywenderlich.emitron.utils.isEqualTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsRepositoryTest {

  private lateinit var settingsRepository: SettingsRepository

  private val settingsPrefs: SettingsPrefs = mock()

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @Before
  fun setUp() {
    settingsRepository = SettingsRepository(settingsPrefs)
  }

  @Test
  fun updateCrashReportingAllowed() {
    settingsRepository.updateCrashReportingAllowed(true)
    verify(settingsPrefs).saveCrashReportingAllowed(true)
    verifyNoMoreInteractions(settingsPrefs)
  }

  @Test
  fun updateNightMode() {
    settingsRepository.updateNightMode(1)
    verify(settingsPrefs).saveNightMode(1)
    verifyNoMoreInteractions(settingsPrefs)
  }

  @Test
  fun getNightMode() {
    whenever(settingsPrefs.getNightMode()).thenReturn(1)
    val result = settingsRepository.getNightMode()
    result isEqualTo 1
    verify(settingsPrefs).getNightMode()
    verifyNoMoreInteractions(settingsPrefs)
  }

  @Test
  fun isCrashReportingAllowed() {
    whenever(settingsPrefs.isCrashReportingAllowed()).thenReturn(true)
    val result = settingsRepository.isCrashReportingAllowed()
    result isEqualTo true
    verify(settingsPrefs).isCrashReportingAllowed()
    verifyNoMoreInteractions(settingsPrefs)
  }
}
