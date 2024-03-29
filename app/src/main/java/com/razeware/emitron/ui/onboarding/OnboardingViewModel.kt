package com.razeware.emitron.ui.onboarding

import androidx.lifecycle.ViewModel
import com.razeware.emitron.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for Onboarding view
 */
@HiltViewModel
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
