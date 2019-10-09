package com.raywenderlich.emitron.ui.onboarding

import androidx.lifecycle.ViewModel
import com.raywenderlich.emitron.data.settings.SettingsRepository
import javax.inject.Inject

/**
 * ViewModel for Onboarding view
 */
class OnboardingViewModel @Inject constructor(
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
