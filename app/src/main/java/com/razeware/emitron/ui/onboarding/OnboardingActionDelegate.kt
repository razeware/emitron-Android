package com.razeware.emitron.ui.onboarding

import com.razeware.emitron.data.settings.SettingsRepository
import javax.inject.Inject

/**
 * Onboarding Actions
 */
interface OnboardingAction {
  /**
   *
   * @param view Onboarding view type [OnboardingView]
   *
   * @return true if onboarding is shown for view, else false
   */
  fun isOnboardedForType(view: OnboardingView): Boolean

  /**
   * @return true if onboarding can be shown, else false
   */
  fun isOnboardingAllowed(): Boolean
}

/**
 * Onboarding action delegate
 */
class OnboardingActionDelegate @Inject constructor(
  private val settingsRepository: SettingsRepository
) :
  OnboardingAction {


  override fun isOnboardedForType(view: OnboardingView): Boolean =
    settingsRepository.getOnboardedViews().contains(view)


  override fun isOnboardingAllowed(): Boolean = settingsRepository.isOnboardingAllowed()
}
