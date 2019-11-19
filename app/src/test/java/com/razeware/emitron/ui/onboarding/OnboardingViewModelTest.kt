package com.razeware.emitron.ui.onboarding

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.razeware.emitron.data.settings.SettingsRepository
import org.junit.Before
import org.junit.Test

class OnboardingViewModelTest {

  private val settingsRepository: SettingsRepository = mock()

  private lateinit var viewModel: OnboardingViewModel

  @Before
  fun setUp() {
    viewModel = OnboardingViewModel(settingsRepository)
  }

  @Test
  fun updateOnboardingAllowed() {
    viewModel.updateOnboardingAllowed()
    verify(settingsRepository).updateOnboardingAllowed(false)
    verifyNoMoreInteractions(settingsRepository)
  }

  @Test
  fun updateOnboardedView() {
    viewModel.updateOnboardedView(OnboardingView.Download)
    verify(settingsRepository).updateOnboardedView(OnboardingView.Download)
    verifyNoMoreInteractions(settingsRepository)
  }
}
