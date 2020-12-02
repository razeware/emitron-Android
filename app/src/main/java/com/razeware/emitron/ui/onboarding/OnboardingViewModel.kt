package com.razeware.emitron.ui.onboarding

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.razeware.emitron.data.settings.SettingsRepository

/**
 * ViewModel for Onboarding view
 */
class OnboardingViewModel @ViewModelInject constructor(
  private val settingsRepository: SettingsRepository
) : ViewModel() {

  /**
   * Update onboarding allowed
   *
   */
  fun updateOnboardingAllowed() {
    settingsRepository.updateOnboardingAllowed(false)
  }

  /**
   * Update onboarded view
   *
   * @param view [OnboardingView]
   */
  fun updateOnboardedView(view: OnboardingView) {
    settingsRepository.updateOnboardedView(view)
  }
}
