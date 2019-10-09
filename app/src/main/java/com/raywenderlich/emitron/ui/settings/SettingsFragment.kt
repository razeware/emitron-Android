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
import androidx.work.WorkManager
import com.crashlytics.android.Crashlytics
import com.raywenderlich.emitron.BuildConfig
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.databinding.FragmentSettingsBinding
import com.raywenderlich.emitron.di.modules.viewmodel.ViewModelFactory
import com.raywenderlich.emitron.ui.login.GuardpostDelegate
import com.raywenderlich.emitron.ui.settings.SettingsBottomSheetDialogFragment.Companion.downloadQualityToResIdMap
import com.raywenderlich.emitron.ui.settings.SettingsBottomSheetDialogFragment.Companion.playbackQualityToResIdMap
import com.raywenderlich.emitron.ui.settings.SettingsBottomSheetDialogFragment.Companion.playbackSpeedToResIdMap
import com.raywenderlich.emitron.ui.settings.SettingsBottomSheetDialogFragment.Companion.playbackSubtitleLanguageToResIdMap
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

  private lateinit var binding: FragmentSettingsBinding

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
    binding = setDataBindingView(R.layout.fragment_settings, container)
    return binding.root
  }


  /**
   * See [androidx.fragment.app.Fragment.onViewCreated]
   */
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initUi()
    initObservers()
  }

  private fun initObservers() {
    val result = guardpostDelegate.registerReceiver()
    result.logout.observe(this, Observer {
      viewModel.logout()
      SignOutWorker.enqueue(WorkManager.getInstance(requireContext()))
      findNavController().navigate(R.id.action_navigation_settings_to_navigation_login)
    })
    viewModel.nightMode.observe(viewLifecycleOwner) {
      it?.let { nightMode ->
        val nightModeResId = when (nightMode) {
          AppCompatDelegate.MODE_NIGHT_YES -> R.string.button_on
          AppCompatDelegate.MODE_NIGHT_NO -> R.string.button_off
          else -> R.string.button_system_default
        }
        binding.settingsSelectedNightMode.text = getString(nightModeResId)
        AppCompatDelegate.setDefaultNightMode(nightMode)
      }
    }
    viewModel.crashReportingAllowed.observe(viewLifecycleOwner) {
      it?.let {
        binding.switchCrashReporting.isChecked = it
        if (it) {
          Fabric.with(requireContext(), Crashlytics())
        }
      }
    }
    viewModel.playbackQuality.observe(viewLifecycleOwner) {
      it?.let { quality ->
        binding.settingsSelectedVideoQuality.text = getString(
          playbackQualityToResIdMap.getOrElse(
            quality
          ) {
            R.string.playback_quality_1080p
          }
        )
      }
    }
    viewModel.playbackSpeed.observe(viewLifecycleOwner) {
      it?.let { speed ->
        binding.settingsSelectedVideoSpeed.text = getString(
          playbackSpeedToResIdMap.getOrElse(
            speed
          ) {
            R.string.playback_speed_normal
          }
        )
      }
    }
    viewModel.subtitlesLanguage.observe(viewLifecycleOwner) {
      it?.let { language ->
        binding.settingsSelectedSubtitleLanguage.text = getString(
          playbackSubtitleLanguageToResIdMap.getOrElse(
            language
          ) {
            R.string.button_off
          })
      }
    }
    viewModel.downloadQuality.observe(viewLifecycleOwner) {
      it?.let { quality ->
        binding.settingsSelectedDownloadQuality.text = getString(
          downloadQualityToResIdMap.getOrElse(
            quality
          ) {
            R.string.download_quality_high
          })
      }
    }
    viewModel.downloadsWifiOnly.observe(viewLifecycleOwner) {
      it?.let {
        binding.switchDownloadNetwork.isChecked = it
      }
    }
  }

  private fun initUi() {
    binding.toolbar.setupWithNavController(
      findNavController(),
      getDefaultAppBarConfiguration()
    )

    binding.titleVersionName.text = BuildConfig.VERSION_NAME
    binding.switchCrashReporting.setOnCheckedChangeListener { _, checked ->
      viewModel.updateCrashReportingAllowed(checked)
    }
    binding.settingsNightMode.setOnClickListener {
      showSettingsBottomSheet(R.string.label_night_mode)
    }
    binding.settingsVideoQuality.setOnClickListener {
      showSettingsBottomSheet(R.string.label_video_playback_quality)
    }
    binding.settingsVideoSpeed.setOnClickListener {
      showSettingsBottomSheet(R.string.label_video_playback_speed)
    }
    binding.settingsSubtitleLanguage.setOnClickListener {
      showSettingsBottomSheet(R.string.label_subtitles)
    }
    binding.switchDownloadNetwork.setOnCheckedChangeListener { _, checked ->
      viewModel.updateDownloadsWifiOnly(checked)
    }
    binding.settingsDownloadQuality.setOnClickListener {
      showSettingsBottomSheet(R.string.label_download_quality)
    }
    binding.buttonLogout.setOnClickListener {
      guardpostDelegate.logout()
    }
    binding.settingsShareApp.setOnClickListener {
      Support.shareApp(requireContext())
    }
    binding.settingsSendFeedback.setOnClickListener {
      Support.sendFeedback(requireContext())
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
