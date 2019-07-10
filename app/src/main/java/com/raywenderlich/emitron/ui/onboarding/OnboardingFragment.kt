package com.raywenderlich.emitron.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.raywenderlich.emitron.R

class OnboardingFragment : Fragment() {

  private lateinit var onboardingViewModel: OnboardingViewModel

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    onboardingViewModel =
      ViewModelProviders.of(this).get(OnboardingViewModel::class.java)
    return inflater.inflate(R.layout.fragment_onboarding, container, false)
  }
}
