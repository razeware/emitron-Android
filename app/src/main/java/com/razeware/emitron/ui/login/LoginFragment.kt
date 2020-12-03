package com.razeware.emitron.ui.login

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.work.WorkManager
import com.razeware.emitron.R
import com.razeware.emitron.databinding.FragmentLoginBinding
import com.razeware.emitron.ui.download.workers.VerifyDownloadWorker
import com.razeware.emitron.utils.extensions.*
import dagger.hilt.android.AndroidEntryPoint

/**
 * Login view
 */
@AndroidEntryPoint
class LoginFragment : Fragment() {

  private val viewModel: LoginViewModel by viewModels()

  private val guardpostDelegate: GuardpostDelegate by lazy {
    GuardpostDelegate(requireContext())
  }

  private lateinit var binding: FragmentLoginBinding

  /**
   * Set up layout
   */
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = setDataBindingView(R.layout.fragment_login, container)
    return binding.root
  }

  /**
   * Set up listeners
   * Set up viewmodel observers
   */
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initView()
    initObservers()
  }

  /**
   * Any time the screen loads, we check if the device supports cutouts and try to adjust our
   * padding accordingly.
   * */
  override fun onResume() {
    super.onResume()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      setupWindowInsets()
    }
  }

  /**
   * Similarly to what we do on the [MainActivity], we add insets to this screen if there's a bottom
   * navigation bar.
   * */
  @TargetApi(Build.VERSION_CODES.P)
  private fun setupWindowInsets() {
    binding.loginRoot.doOnLayout {
      val inset = binding.loginRoot.rootWindowInsets

      val cutoutSize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        inset?.getInsets(WindowInsets.Type.navigationBars())?.bottom
      } else {
        inset?.displayCutout?.safeInsetBottom
      }

      if (cutoutSize != null) {
        binding.bottomPadding = cutoutSize
      }
    }
  }

  private fun initObservers() {
    val result = guardpostDelegate.registerReceiver()

    result.login.observe(this) { user ->
      user?.let {
        viewModel.storeUser(user)
        checkPermissions()
      }
    }

    result.logout.observe(this) {
      viewModel.deleteUser()
      handleNoSubscription(false)
    }

    viewModel.permissionActionResult.observe(viewLifecycleOwner) {
      binding.buttonSignIn.isEnabled = true
      when (it) {
        PermissionActionDelegate.PermissionActionResult.HasPermission -> {
          if (viewModel.isDownloadAllowed()) {
            VerifyDownloadWorker.queue(WorkManager.getInstance(requireContext()))
          }
          findNavController().navigate(R.id.action_navigation_login_to_navigation_library)
        }
        PermissionActionDelegate.PermissionActionResult.NoPermission -> {
          handleNoSubscription()
        }
        PermissionActionDelegate.PermissionActionResult.PermissionRequestFailed ->
          showErrorSnackbar(getString(R.string.error_login_subscription))
        else -> {
          // Will be handled by data binding.
        }
      }
    }
  }

  private fun initView() {
    with(binding) {
      textLoginErrorDescription.removeUnderline()

      buttonSignIn.setOnClickListener {
        guardpostDelegate.login()
      }
      buttonSignOut.setOnClickListener {
        guardpostDelegate.logout()
      }
      viewPagerLogin.adapter =
        LoginOnboardingPagingAdapter.newInstance(requireFragmentManager())
      viewPagerIndicator.attachViewPager(binding.viewPagerLogin)
    }
  }

  private fun handleNoSubscription(show: Boolean = true) {
    with(binding) {
      layoutLogin.isVisible = !show
      layoutLoginNoSubscription.isVisible = show
    }
  }

  /**
   * Clear guardpost delegate
   */
  override fun onDestroy() {
    super.onDestroy()
    guardpostDelegate.clear()
  }

  private fun checkPermissions() {
    if (isNetNotConnected()) {
      showErrorSnackbar(getString(R.string.error_no_connection))
      return
    }

    binding.buttonSignIn.isEnabled = false
    viewModel.getPermissions()
  }
}
