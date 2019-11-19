package com.razeware.emitron.ui.onboarding

import com.nhaarman.mockitokotlin2.*
import com.razeware.emitron.data.settings.SettingsRepository
import com.razeware.emitron.utils.isEqualTo
import org.junit.Before
import org.junit.Test

class OnboardingActionDelegateTest {

  private val settingsRepository: SettingsRepository = mock()

  private lateinit var viewModel: OnboardingActionDelegate

  @Before
  fun setUp() {
    viewModel = OnboardingActionDelegate(settingsRepository)
  }

  @Test
  fun isOnboardedForType() {
    whenever(settingsRepository.getOnboardedViews()).doReturn(listOf(OnboardingView.Download))

    val result = viewModel.isOnboardedForType(OnboardingView.Download)
    result isEqualTo true
    verify(settingsRepository).getOnboardedViews()
    verifyNoMoreInteractions(settingsRepository)
  }

  @Test
  fun isOnboardingAllowed() {
    whenever(settingsRepository.isOnboardingAllowed()).doReturn(true)

    val result = viewModel.isOnboardingAllowed()
    result isEqualTo true
    verify(settingsRepository).isOnboardingAllowed()
    verifyNoMoreInteractions(settingsRepository)
  }
}
