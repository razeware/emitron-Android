package com.razeware.emitron.ui.settings

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.navigation.ui.setupWithNavController
import androidx.work.WorkManager
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.razeware.emitron.BuildConfig
import com.razeware.emitron.R
import com.razeware.emitron.databinding.FragmentSettingsBinding
import com.razeware.emitron.ui.common.getDefaultAppBarConfiguration
import com.razeware.emitron.ui.download.workers.PendingDownloadWorker
import com.razeware.emitron.ui.login.GuardpostDelegate
import com.razeware.emitron.ui.settings.SettingsBottomSheetDialogFragment.Companion.downloadQualityToResIdMap
import com.razeware.emitron.ui.settings.SettingsBottomSheetDialogFragment.Companion.playbackQualityToResIdMap
import com.razeware.emitron.ui.settings.SettingsBottomSheetDialogFragment.Companion.playbackSpeedToResIdMap
import com.razeware.emitron.ui.settings.SettingsBottomSheetDialogFragment.Companion.playbackSubtitleLanguageToResIdMap
import com.razeware.emitron.utils.extensions.observe
import com.razeware.emitron.utils.extensions.setDataBindingView
import dagger.hilt.android.AndroidEntryPoint

/**
 * Settings UI
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {

  private val viewModel: SettingsViewModel by navGraphViewModels(R.id.settings_navigation) {
    defaultViewModelProviderFactory
  }

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
    viewModel.init()
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
    binding.settingsFooter.doOnLayout {
      val inset = binding.settingsFooter.rootWindowInsets

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
    result.logout.observe(this, Observer {
      viewModel.logout()
      SignOutWorker.enqueue(WorkManager.getInstance(requireContext()))
      findNavController().navigate(R.id.action_navigation_settings_to_navigation_login)
    })

    with(viewModel) {
      nightMode.observe(viewLifecycleOwner) {
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
      crashReportingAllowed.observe(viewLifecycleOwner) {
        it?.let { hasUserEnabledCrashReporting ->
          binding.switchCrashReporting.isChecked = hasUserEnabledCrashReporting

          FirebaseCrashlytics
            .getInstance()
            .setCrashlyticsCollectionEnabled(hasUserEnabledCrashReporting)
        }
      }
      playbackQuality.observe(viewLifecycleOwner) {
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
      playbackSpeed.observe(viewLifecycleOwner) {
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
      subtitlesLanguage.observe(viewLifecycleOwner) {
        it?.let { language ->
          binding.settingsSelectedSubtitleLanguage.text = getString(
            playbackSubtitleLanguageToResIdMap.getOrElse(
              language
            ) {
              R.string.button_off
            })
        }
      }
      downloadQuality.observe(viewLifecycleOwner) {
        it?.let { quality ->
          binding.settingsSelectedDownloadQuality.text = getString(
            downloadQualityToResIdMap.getOrElse(
              quality
            ) {
              R.string.download_quality_high
            })
        }
      }
      downloadsWifiOnly.observe(viewLifecycleOwner) {
        it?.let {
          binding.switchDownloadNetwork.isChecked = it
        }
      }
    }
  }

  private fun initUi() {
    with(binding) {
      toolbar.setupWithNavController(
        findNavController(),
        getDefaultAppBarConfiguration()
      )

      titleVersionName.text = getString(
        R.string.label_version, BuildConfig.VERSION_NAME
      )
      titleLoggedInUser.text = getString(
        R.string.settings_logged_in_user, viewModel.getLoggedInUser()
      )
      switchCrashReporting.setOnCheckedChangeListener { _, checked ->
        viewModel.updateCrashReportingAllowed(checked)
      }
      settingsNightMode.setOnClickListener {
        showSettingsBottomSheet(R.string.label_night_mode)
      }
      settingsVideoQuality.setOnClickListener {
        showSettingsBottomSheet(R.string.label_video_playback_quality)
      }
      settingsVideoSpeed.setOnClickListener {
        showSettingsBottomSheet(R.string.label_video_playback_speed)
      }
      settingsSubtitleLanguage.setOnClickListener {
        showSettingsBottomSheet(R.string.label_subtitles)
      }
      switchDownloadNetwork.setOnCheckedChangeListener { _, checked ->
        viewModel.updateDownloadsWifiOnly(checked)
        if (!checked) {
          PendingDownloadWorker.enqueue(
            WorkManager.getInstance(requireContext()),
            viewModel.getDownloadsWifiOnly()
          )
        }
      }
      settingsDownloadQuality.setOnClickListener {
        showSettingsBottomSheet(R.string.label_download_quality)
      }
      buttonLogout.setOnClickListener {
        guardpostDelegate.logout()
      }
      settingsShareApp.setOnClickListener {
        Support.shareApp(requireContext())
      }
      settingsSendFeedback.setOnClickListener {
        Support.sendFeedback(requireContext())
      }
      settingsOssLicenses.setOnClickListener {
        startActivity(Intent(requireActivity(), OssLicensesMenuActivity::class.java))
      }
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
