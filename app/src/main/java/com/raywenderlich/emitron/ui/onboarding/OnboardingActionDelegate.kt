package com.raywenderlich.emitron.ui.onboarding

import com.raywenderlich.emitron.data.settings.SettingsRepository
import javax.inject.Inject

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

class OnboardingActionDelegate @Inject constructor(
  private val settingsRepository: SettingsRepository
) :
  OnboardingAction {


  override fun isOnboardedForType(view: OnboardingView): Boolean =
    settingsRepository.getOnboardedViews().contains(view)


  override fun isOnboardingAllowed(): Boolean = settingsRepository.isOnboardingAllowed()
}
