package com.raywenderlich.emitron.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.databinding.FragmentSettingsBottomsheetBinding
import com.raywenderlich.emitron.di.modules.viewmodel.ViewModelFactory
import com.raywenderlich.emitron.ui.player.PlayerFragment.Companion.subtitleLanguageEnglish
import com.raywenderlich.emitron.utils.extensions.setDataBindingView
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * Settings UI
 */
class SettingsBottomSheetDialogFragment : BottomSheetDialogFragment() {

  companion object {

    /**
     * Create and show
     *
     * @param navController NavController used to show
     * @param headerResId Res id for header
     */
    fun show(
      navController: NavController,
      @StringRes headerResId: Int
    ) {
      val settingsBottomSheetDirection =
        SettingsFragmentDirections
          .actionNavigationSettingsBottomSheet(headerResId)
      navController.navigate(settingsBottomSheetDirection)
    }

    /**
     * Map of allowed night mode values to respective [@StringRes]
     */
    val nightModeToResIdMap: Map<Int, Int> by lazy {
      mapOf(
        AppCompatDelegate.MODE_NIGHT_NO to R.string.button_off,
        AppCompatDelegate.MODE_NIGHT_YES to R.string.button_on,
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM to R.string.button_system_default
      )
    }

    /**
     * Map of allowed subtitle languages to respective [@StringRes]
     */
    val playbackSubtitleLanguageToResIdMap: Map<String, Int> by lazy {
      mapOf(
        "" to R.string.button_off,
        subtitleLanguageEnglish to R.string.button_player_label_english
      )
    }

    /**
     * List of allowed playback quality values
     *
     * (In order of visibility)
     */
    val settingsPlaybackQualityOptions: List<Int> by lazy {
      listOf(
        R.string.playback_quality_1080p_recommended,
        R.string.playback_quality_720p,
        R.string.playback_quality_540p,
        R.string.playback_quality_360p,
        R.string.playback_quality_240p,
        R.string.playback_quality_auto
      )
    }

    /**
     * List of allowed playback speed values
     *
     * (In order of visibility)
     */
    val settingsPlaybackSpeedOptions: List<Int> by lazy {
      listOf(
        R.string.playback_speed_normal,
        R.string.playback_speed_0_5x,
        R.string.playback_speed_0_75x,
        R.string.playback_speed_1_25x,
        R.string.playback_speed_1_5x,
        R.string.playback_speed_2x
      )
    }

    /**
     * List of allowed subtitle language values
     *
     * (In order of visibility)
     */
    val settingsSubtitleLanguageOptions: List<Int> by lazy {
      listOf(
        R.string.button_off,
        R.string.button_player_label_english
      )
    }

    /**
     * List of allowed night mode values
     *
     * (In order of visibility)
     */
    val settingsNightModeOptions: List<Int> by lazy {
      listOf(
        R.string.button_on,
        R.string.button_off,
        R.string.button_system_default
      )
    }

    /**
     * Map of allowed playback quality to respective [@StringRes]
     */
    val playbackQualityToResIdMap: Map<Int, Int> by lazy {
      mapOf(
        1080 to R.string.playback_quality_1080p_recommended,
        720 to R.string.playback_quality_720p,
        540 to R.string.playback_quality_540p,
        360 to R.string.playback_quality_360p,
        240 to R.string.playback_quality_240p,
        1 to R.string.playback_quality_auto
      )
    }

    /**
     * Map of allowed playback speed to respective [@StringRes]
     */
    val playbackSpeedToResIdMap: Map<Float, Int> by lazy {
      mapOf(
        1f to R.string.playback_speed_normal,
        0.5f to R.string.playback_speed_0_5x,
        0.75f to R.string.playback_speed_0_75x,
        1.25f to R.string.playback_speed_1_25x,
        1.5f to R.string.playback_speed_1_5x,
        2f to R.string.playback_speed_2x
      )
    }
  }

  /**
   * Custom factory for viewmodel
   *
   * Custom factory provides app related dependencies
   */
  @Inject
  lateinit var viewModelFactory: ViewModelFactory

  private lateinit var viewBinding: FragmentSettingsBottomsheetBinding

  private val args by navArgs<SettingsBottomSheetDialogFragmentArgs>()

  private val viewModel:
      SettingsViewModel by navGraphViewModels(R.id.settings_navigation) { viewModelFactory }

  /**
   * See [androidx.fragment.app.Fragment.onCreateView]
   */
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    viewBinding = setDataBindingView(inflater, R.layout.fragment_settings_bottomsheet, container)
    return viewBinding.root
  }

  /**
   * See [androidx.fragment.app.Fragment.onViewCreated]
   */
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val headerResId = args.header
    if (headerResId != 0) {
      viewBinding.titleSettingsBottomSheet.text = getString(headerResId)
    }
    with(viewBinding.recyclerView) {
      layoutManager = object : LinearLayoutManager(requireContext()) {
        override fun canScrollVertically(): Boolean = false
      }
      adapter = SettingsBottomSheetAdapter(getSettingsOptions(headerResId)) {
        onSettingsChange(headerResId, it)
      }
    }
  }

  /**
   * See [androidx.fragment.app.Fragment.onAttach]
   */
  override fun onAttach(context: Context) {
    AndroidSupportInjection.inject(this)
    super.onAttach(context)
  }

  private fun getSettingsOptions(@StringRes headerResId: Int): List<Pair<String, Boolean>> {
    return when (headerResId) {
      R.string.label_night_mode -> {
        settingsNightModeOptions.map {
          getString(it) to (nightModeToResIdMap.getOrElse(
            viewModel.getNightMode(),
            { R.string.button_system_default }) == it)
        }
      }
      R.string.label_video_playback_quality -> {
        settingsPlaybackQualityOptions.map {
          getString(it) to (playbackQualityToResIdMap.getOrElse(
            viewModel.getPlaybackQuality(),
            { R.string.button_player_auto }) == it)
        }
      }
      R.string.label_video_playback_speed -> {
        settingsPlaybackSpeedOptions.map {
          getString(it) to (playbackSpeedToResIdMap.getOrElse(
            viewModel.getPlaybackSpeed(),
            { R.string.button_system_default }) == it)
        }
      }
      R.string.label_subtitles -> {
        settingsSubtitleLanguageOptions.map {
          getString(it) to (playbackSubtitleLanguageToResIdMap.getOrElse(
            viewModel.getSubtitleLanguage(),
            { R.string.button_off }) == it)
        }
      }
      else -> {
        settingsNightModeOptions.map {
          getString(it) to (nightModeToResIdMap.getOrElse(
            it,
            { R.string.button_system_default }) == it)
        }
      }
    }
  }

  private fun onSettingsChange(
    @StringRes headerResId: Int,
    position: Int
  ) {
    when (headerResId) {
      R.string.label_night_mode -> {
        updateNightModeSettings(position, settingsNightModeOptions)
      }
      R.string.label_video_playback_quality -> {
        updatePlaybackQualitySettings(position, settingsPlaybackQualityOptions)
      }
      R.string.label_video_playback_speed -> {
        updatePlaybackSpeedSettings(position, settingsPlaybackSpeedOptions)
      }
      R.string.label_subtitles -> {
        updateSubtitleLanguage(position, settingsSubtitleLanguageOptions)
      }
    }
  }

  private fun updatePlaybackSpeedSettings(position: Int, options: List<Int>) {
    val playbackSpeed = when (options[position]) {
      R.string.playback_speed_0_5x -> {
        0.5f
      }
      R.string.playback_speed_0_75x -> {
        0.75f
      }
      R.string.playback_speed_1_25x -> {
        1.25f
      }
      R.string.playback_speed_1_5x -> {
        1.5f
      }
      R.string.playback_speed_2x -> {
        2f
      }
      else -> 1f
    }
    viewModel.updatePlaybackSpeed(playbackSpeed)
    dismiss()
  }

  private fun updatePlaybackQualitySettings(position: Int, options: List<Int>) {
    val playbackQuality = when (options[position]) {
      R.string.playback_quality_720p -> {
        720
      }
      R.string.playback_quality_540p -> {
        540
      }
      R.string.playback_quality_360p -> {
        360
      }
      R.string.playback_quality_240p -> {
        240
      }
      R.string.playback_quality_auto -> {
        1
      }
      else -> 1080
    }
    viewModel.updatePlaybackQuality(playbackQuality)
    dismiss()
  }

  /**
   * @param position selected settings option
   *
   */
  private fun updateNightModeSettings(position: Int, options: List<Int>) {
    val nightMode = when (options[position]) {
      R.string.button_off -> {
        AppCompatDelegate.MODE_NIGHT_NO
      }
      R.string.button_on -> {
        AppCompatDelegate.MODE_NIGHT_YES
      }
      else -> {
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
      }
    }
    viewModel.updateNightMode(nightMode)
    dismiss()
  }

  private fun updateSubtitleLanguage(position: Int, options: List<Int>) {
    val subtitleLanguage = when (options[position]) {
      R.string.button_player_label_english -> {
        "en"
      }
      else -> {
        ""
      }
    }
    viewModel.updateSubtitlesLanguage(subtitleLanguage)
    dismiss()
  }
}
