package com.razeware.emitron.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.razeware.emitron.R
import com.razeware.emitron.databinding.FragmentLoginOnboardingBinding
import com.razeware.emitron.utils.extensions.setDataBindingView

/**
 * Login onboarding view
 */
class LoginOnboardingFragment : Fragment() {

  companion object {
    private const val EXTRA_POSITION: String = "extra_position"
    private const val DEFAULT_ONBOARDING_POSITION: Int = 0

    /**
     * Create new instance of [LoginOnboardingFragment]
     */
    fun newInstance(position: Int): Fragment {
      return LoginOnboardingFragment().apply {
        arguments = bundleOf(EXTRA_POSITION to position)
      }
    }
  }

  private lateinit var binding: FragmentLoginOnboardingBinding

  /**
   * Set up layout
   */
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = setDataBindingView(R.layout.fragment_login_onboarding, container)
    return binding.root
  }

  /**
   * Set up listeners
   * Set up viewmodel observers
   */
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initView()
  }

  private fun initView() {
    val position = arguments?.getInt(
      EXTRA_POSITION,
      DEFAULT_ONBOARDING_POSITION
    ) ?: DEFAULT_ONBOARDING_POSITION

    when (position) {
      DEFAULT_ONBOARDING_POSITION -> {
        with(binding) {
          loginOnboardingTitle.text = getString(R.string.title_login_onboarding_1)
          loginOnboardingBody.text = getString(R.string.body_login_onboarding_1)
          loginOnboardingImage.setImageResource(R.drawable.ic_login_onboarding_1)
        }
      }
      else -> {
        with(binding) {
          loginOnboardingTitle.text = getString(R.string.title_login_onboarding_2)
          loginOnboardingBody.text = getString(R.string.body_login_onboarding_2)
          loginOnboardingImage.setImageResource(R.drawable.ic_login_onboarding_2)
        }
      }
    }
  }
}
