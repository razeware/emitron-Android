package com.raywenderlich.emitron.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.raywenderlich.emitron.BuildConfig
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.databinding.FragmentSettingsBinding
import com.raywenderlich.emitron.di.modules.viewmodel.ViewModelFactory
import com.raywenderlich.emitron.ui.login.GuardpostDelegate
import com.raywenderlich.emitron.utils.extensions.setDataBindingView
import com.raywenderlich.emitron.utils.getDefaultAppBarConfiguration
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject

/**
 * Settings UI
 */
class SettingsFragment : DaggerFragment() {

  /**
   * Custom factory for viewmodel
   *
   * Custom factory provides app related dependencies
   */
  @Inject
  lateinit var viewModelFactory: ViewModelFactory

  private val viewModel: SettingsViewModel by viewModels { viewModelFactory }

  private lateinit var viewBinding: FragmentSettingsBinding

  private val guardpostDelegate: GuardpostDelegate by lazy {
    GuardpostDelegate(requireContext())
  }

  /**
   * Set up layout
   */
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    viewBinding = setDataBindingView(inflater, R.layout.fragment_settings, container)
    return viewBinding.root
  }

  /**
   * Set up listeners
   * Set up viewmodel observers
   */
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewBinding.toolbar.setupWithNavController(
      findNavController(),
      getDefaultAppBarConfiguration()
    )
    val result = guardpostDelegate.registerReceiver()
    result.logout.observe(this, Observer {
      viewModel.logout()
      findNavController().navigate(R.id.action_navigation_settings_to_navigation_login)
    })
    button_logout.setOnClickListener {
      guardpostDelegate.logout()
    }
    title_version_name.text = BuildConfig.VERSION_NAME
  }

  /**
   * Clear guardpost delegate
   */
  override fun onDestroy() {
    super.onDestroy()
    guardpostDelegate.clear()
  }
}
