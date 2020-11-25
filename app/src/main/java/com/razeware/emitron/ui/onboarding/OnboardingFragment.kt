package com.razeware.emitron.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.razeware.emitron.R
import com.razeware.emitron.databinding.FragmentOnboardingBinding
import com.razeware.emitron.ui.common.getDefaultAppBarConfiguration
import com.razeware.emitron.utils.extensions.applyUnderline
import com.razeware.emitron.utils.extensions.setDataBindingView
import dagger.hilt.android.AndroidEntryPoint

/**
 * Onboarding View
 */
@AndroidEntryPoint
class OnboardingFragment : Fragment() {

  private val viewModel: OnboardingViewModel by viewModels()

  private val args by navArgs<OnboardingFragmentArgs>()

  private lateinit var binding: FragmentOnboardingBinding

  /**
   * See [androidx.fragment.app.Fragment.onCreateView]
   */
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = setDataBindingView(R.layout.fragment_onboarding, container)
    return binding.root
  }

  /**
   * See [androidx.fragment.app.Fragment.onViewCreated]
   */
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initToolbar()
    initUi()
  }

  private fun initToolbar() {
    binding.toolbar.setupWithNavController(
      findNavController(),
      getDefaultAppBarConfiguration()
    )
    binding.toolbar.navigationIcon =
      VectorDrawableCompat.create(resources, R.drawable.ic_material_icon_close, null)
  }

  private fun initUi() {
    val onboardingView = args.onboardingView
    viewModel.updateOnboardedView(onboardingView)
    when (onboardingView) {
      OnboardingView.Download -> {
        with(binding) {
          onBoardingTitle.text = getString(R.string.on_boarding_download_title)
          onBoardingBody.text = getString(R.string.on_boarding_download_body)
          onboardingStubDownload.viewStub?.isVisible = true
        }
      }
      OnboardingView.Collection -> {
        with(binding) {
          onBoardingTitle.text = getString(R.string.on_boarding_collection_title)
          onBoardingBody.text = getString(R.string.on_boarding_collection_body)
          onboardingStubCollection.viewStub?.isVisible = true
        }
      }
    }

    with(binding) {
      buttonOnBoardingClose.text = getString(R.string.button_label_on_boarding_close)
      buttonOnBoardingClose.applyUnderline()
      buttonOnBoardingClose.setOnClickListener {
        viewModel.updateOnboardingAllowed()
        findNavController().popBackStack()
      }
      buttonOnBoardingSubmit.setOnClickListener {
        findNavController().popBackStack()
      }
    }

    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
      viewModel.updateOnboardedView(onboardingView)
    }
  }
}
