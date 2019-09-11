package com.raywenderlich.emitron.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.navigation.ui.setupWithNavController
import com.crashlytics.android.Crashlytics
import com.raywenderlich.emitron.BuildConfig
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.databinding.FragmentSettingsBinding
import com.raywenderlich.emitron.di.modules.viewmodel.ViewModelFactory
import com.raywenderlich.emitron.ui.login.GuardpostDelegate
import com.raywenderlich.emitron.utils.extensions.observe
import com.raywenderlich.emitron.utils.extensions.setDataBindingView
import com.raywenderlich.emitron.utils.getDefaultAppBarConfiguration
import dagger.android.support.DaggerFragment
import io.fabric.sdk.android.Fabric
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

  private val viewModel:
      SettingsViewModel by navGraphViewModels(R.id.settings_navigation) { viewModelFactory }

  private lateinit var viewBinding: FragmentSettingsBinding

  private val guardpostDelegate: GuardpostDelegate by lazy {
    GuardpostDelegate(requireContext())
  }

  /**
   * See [androidx.fragment.app.Fragment.onCreateView]
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
   * See [androidx.fragment.app.Fragment.onViewCreated]
   */
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initToolbar()
    initUi()
    initObservers()
  }

  private fun initToolbar() {
    viewBinding.toolbar.setupWithNavController(
      findNavController(),
      getDefaultAppBarConfiguration()
    )
  }

  private fun initObservers() {
    val result = guardpostDelegate.registerReceiver()
    result.logout.observe(this, Observer {
      viewModel.logout()
      findNavController().navigate(R.id.action_navigation_settings_to_navigation_login)
    })
    viewModel.nightMode.observe(viewLifecycleOwner) {
      it?.let { nightMode ->
        val nightModeResId = when (nightMode) {
          AppCompatDelegate.MODE_NIGHT_YES -> R.string.button_on
          AppCompatDelegate.MODE_NIGHT_NO -> R.string.button_off
          else -> R.string.button_system_default
        }
        viewBinding.settingsSelectedNightMode.text = getString(nightModeResId)
        AppCompatDelegate.setDefaultNightMode(nightMode)
      }
    }
    viewModel.crashReportingAllowed.observe(viewLifecycleOwner) {
      it?.let {
        viewBinding.switchCrashReporting.isChecked = it
        if (it) {
          Fabric.with(requireContext(), Crashlytics())
        }
      }
    }
  }

  private fun initUi() {
    viewBinding.titleVersionName.text = BuildConfig.VERSION_NAME
    viewBinding.switchCrashReporting.setOnCheckedChangeListener { _, checked ->
      viewModel.updateCrashReportingAllowed(checked)
    }
    viewBinding.settingsNightMode.setOnClickListener {
      showSettingsBottomSheet(R.string.label_night_mode)
    }
  }

  private fun showSettingsBottomSheet(@StringRes headerResId: Int) {
    SettingsBottomSheetDialogFragment.show(findNavController(), headerResId)
  }

  /**
   * Clear guardpost delegate
   */
  override fun onDestroy() {
    super.onDestroy()
    guardpostDelegate.clear()
  }
}
