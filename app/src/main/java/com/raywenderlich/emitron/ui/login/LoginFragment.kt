package com.raywenderlich.emitron.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.databinding.FragmentLoginBinding
import com.raywenderlich.emitron.di.modules.viewmodel.ViewModelFactory
import com.raywenderlich.emitron.utils.extensions.*
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * Login view
 */
class LoginFragment : DaggerFragment() {

  /**
   * Custom factory for viewmodel
   *
   * Custom factory provides app related dependencies
   */
  @Inject
  lateinit var viewModelFactory: ViewModelFactory

  private val viewModel: LoginViewModel by viewModels { viewModelFactory }

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
    binding = setDataBindingView(inflater, R.layout.fragment_login, container)
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

  private fun initObservers() {
    val result = guardpostDelegate.registerReceiver()

    val showLoginScreen = {
      binding.layoutLogin.visibility = View.GONE
      binding.layoutLoginNoSubscription.visibility = View.VISIBLE
    }
    result.login.observe(this) { user ->
      user?.let {
        viewModel.storeUser(user)
        checkSubscription()
      }
    }

    result.logout.observe(this) {
      viewModel.deleteUser()
      showLoginScreen()
    }

    viewModel.loginActionResult.observe(viewLifecycleOwner) {
      binding.buttonSignIn.isEnabled = true
      when (it) {
        LoginViewModel.LoginActionResult.LoggedIn ->
          findNavController().navigate(R.id.action_navigation_login_to_navigation_library)
        LoginViewModel.LoginActionResult.NoSubscription -> {
          showLoginScreen()
        }
        LoginViewModel.LoginActionResult.SubscriptionRequestFailed ->
          showErrorSnackbar(getString(R.string.error_login_subscription))
        else -> {
          // Will be handled by data binding.
        }
      }
    }
  }

  private fun initView() {
    binding.textLoginErrorDescription.removeUnderline()

    binding.buttonSignIn.setOnClickListener {
      guardpostDelegate.login()
    }
    binding.buttonSignOut.setOnClickListener {
      guardpostDelegate.logout()
    }
  }

  /**
   * Clear guardpost delegate
   */
  override fun onDestroy() {
    super.onDestroy()
    guardpostDelegate.clear()
  }

  private fun checkSubscription() {
    if (isNetNotConnected()) {
      showErrorSnackbar(getString(R.string.error_no_connection))
      return
    }

    binding.buttonSignIn.isEnabled = false
    viewModel.getSubscription()
  }
}
