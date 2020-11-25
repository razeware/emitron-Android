package com.razeware.emitron.ui.settings

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
import com.razeware.emitron.R
import com.razeware.emitron.databinding.FragmentSettingsBottomsheetBinding
import com.razeware.emitron.model.DownloadQuality
import com.razeware.emitron.ui.player.PlayerFragment.Companion.subtitleLanguageEnglish
import com.razeware.emitron.ui.player.PlayerFragment.Playback.Quality.AUTO
import com.razeware.emitron.ui.player.PlayerFragment.Playback.Quality.FHD
import com.razeware.emitron.ui.player.PlayerFragment.Playback.Quality.HD
import com.razeware.emitron.ui.player.PlayerFragment.Playback.Quality.NHD
import com.razeware.emitron.ui.player.PlayerFragment.Playback.Quality.QHD
import com.razeware.emitron.ui.player.PlayerFragment.Playback.Quality.QVGA
import com.razeware.emitron.ui.player.PlayerFragment.Playback.Speed.DOUBLE
import com.razeware.emitron.ui.player.PlayerFragment.Playback.Speed.HALF
import com.razeware.emitron.ui.player.PlayerFragment.Playback.Speed.NORMAL
import com.razeware.emitron.ui.player.PlayerFragment.Playback.Speed.NORMALx0_75
import com.razeware.emitron.ui.player.PlayerFragment.Playback.Speed.NORMALx1_25
import com.razeware.emitron.ui.player.PlayerFragment.Playback.Speed.NORMALx1_50
import com.razeware.emitron.ui.player.PlayerFragment.Playback.Subtitle.ENGLISH
import com.razeware.emitron.utils.extensions.setDataBindingView
import dagger.hilt.android.AndroidEntryPoint

/**
 * Settings UI
 */
@AndroidEntryPoint
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
        FHD to R.string.playback_quality_1080p,
        HD to R.string.playback_quality_720p,
        QHD to R.string.playback_quality_540p,
        NHD to R.string.playback_quality_360p,
        QVGA to R.string.playback_quality_240p,
        AUTO to R.string.playback_quality_auto
      )
    }

    /**
     * Map of allowed playback speed to respective [@StringRes]
     */
    val playbackSpeedToResIdMap: Map<Float, Int> by lazy {
      mapOf(
        NORMAL to R.string.playback_speed_normal,
        HALF to R.string.playback_speed_0_5x,
        NORMALx0_75 to R.string.playback_speed_0_75x,
        NORMALx1_25 to R.string.playback_speed_1_25x,
        NORMALx1_50 to R.string.playback_speed_1_5x,
        DOUBLE to R.string.playback_speed_2x
      )
    }

    /**
     * List of allowed download quality values
     *
     * (In order of visibility)
     */
    val settingsDownloadQualityOptions: List<Int> by lazy {
      listOf(
        R.string.download_quality_high,
        R.string.download_quality_normal
      )
    }

    /**
     * Map of allowed download quality to respective [@StringRes]
     */
    val downloadQualityToResIdMap: Map<String, Int> by lazy {
      mapOf(
        DownloadQuality.HD.pref to R.string.download_quality_high,
        DownloadQuality.SD.pref to R.string.download_quality_normal
      )
    }
  }

  private lateinit var viewBinding: FragmentSettingsBottomsheetBinding

  private val args by navArgs<SettingsBottomSheetDialogFragmentArgs>()

  private val viewModel:
      SettingsViewModel by navGraphViewModels(R.id.settings_navigation) {
    defaultViewModelProviderFactory
  }

  /**
   * See [androidx.fragment.app.Fragment.onCreateView]
   */
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    viewBinding = setDataBindingView(R.layout.fragment_settings_bottomsheet, container)
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
      R.string.label_download_quality -> {
        settingsDownloadQualityOptions.map {
          getString(it) to (downloadQualityToResIdMap.getOrElse(
            viewModel.getDownloadQuality(),
            { R.string.download_quality_high }) == it)
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
      R.string.label_download_quality -> {
        updateDownloadQuality(position, settingsDownloadQualityOptions)
      }
    }
  }

  private fun updatePlaybackSpeedSettings(position: Int, options: List<Int>) {
    val playbackSpeed = when (options[position]) {
      R.string.playback_speed_0_5x -> {
        HALF
      }
      R.string.playback_speed_0_75x -> {
        NORMALx0_75
      }
      R.string.playback_speed_1_25x -> {
        NORMALx1_25
      }
      R.string.playback_speed_1_5x -> {
        NORMALx1_50
      }
      R.string.playback_speed_2x -> {
        DOUBLE
      }
      else -> NORMAL
    }
    viewModel.updatePlaybackSpeed(playbackSpeed)
    dismiss()
  }

  private fun updatePlaybackQualitySettings(position: Int, options: List<Int>) {
    val playbackQuality = when (options[position]) {
      R.string.playback_quality_720p -> {
        HD
      }
      R.string.playback_quality_540p -> {
        QHD
      }
      R.string.playback_quality_360p -> {
        NHD
      }
      R.string.playback_quality_240p -> {
        QVGA
      }
      R.string.playback_quality_auto -> {
        AUTO
      }
      else -> FHD
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
        ENGLISH
      }
      else -> {
        ""
      }
    }
    viewModel.updateSubtitlesLanguage(subtitleLanguage)
    dismiss()
  }

  private fun updateDownloadQuality(position: Int, options: List<Int>) {
    val quality = when (options[position]) {
      R.string.download_quality_high -> {
        DownloadQuality.HD.pref
      }
      else -> {
        DownloadQuality.SD.pref
      }
    }
    viewModel.updateDownloadQuality(quality)
    dismiss()
  }
}
