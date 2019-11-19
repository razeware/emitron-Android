package com.razeware.emitron.ui.login

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

/**
 * Login onboarding adapter
 */
class LoginOnboardingPagingAdapter(fragmentManager: FragmentManager) :
  FragmentStatePagerAdapter(
    fragmentManager,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
  ) {

  companion object {
    private const val LOGIN_ONBOARDING_COUNT: Int = 2

    /**
     * Create new instance of [LoginOnboardingPagingAdapter]
     */
    fun newInstance(fragmentManager: FragmentManager): FragmentStatePagerAdapter =
      LoginOnboardingPagingAdapter(fragmentManager)
  }

  /**
   * See [FragmentStatePagerAdapter.getItem]
   */
  override fun getItem(position: Int): Fragment = LoginOnboardingFragment.newInstance(position)

  /**
   * See [FragmentStatePagerAdapter.getCount]
   */
  override fun getCount(): Int = LOGIN_ONBOARDING_COUNT

}
