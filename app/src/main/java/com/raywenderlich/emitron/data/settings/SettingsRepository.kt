package com.raywenderlich.emitron.data.settings

import javax.inject.Inject

/**
 * Repository for settings data
 */
class SettingsRepository @Inject constructor(private val settingsPrefs: SettingsPrefs) {

  /**
   * Store crash reporting allowed by user in preference
   *
   * @param allowed True if user has allowed to store crash reporting false otherwise.
   */
  fun updateCrashReportingAllowed(allowed: Boolean) {
    settingsPrefs.saveCrashReportingAllowed(allowed)
  }

  /**
   * Store user night mode choice in preference
   *
   * @param nightMode Current night mode setting
   */
  fun updateNightMode(nightMode: Int) {
    settingsPrefs.saveNightMode(nightMode)
  }

  /**
   * Get user stored night mode from preferences
   *
   * @return current night mode
   */
  fun getNightMode(): Int {
    return settingsPrefs.getNightMode()
  }

  /**
   * Get if user allowed to report crashes
   *
   * @return True, if user has allowed crash reporting, else False
   */
  fun isCrashReportingAllowed(): Boolean {
    return settingsPrefs.isCrashReportingAllowed()
  }

  /**
   * Store user auto play next choice in preference
   *
   * @param allowed Auto play next
   */
  fun updateAutoPlaybackAllowed(allowed: Boolean) {
    settingsPrefs.saveAutoPlayAllowed(allowed)
  }

  /**
   * Store user subtitle language selection in preference
   *
   * (language should be stored in ISO language code format)
   *
   * @param language User language selection
   */
  fun updateSubtitleLanguage(language: String) {
    settingsPrefs.saveSubtitleLanguage(language.toLowerCase()) // Ensure lowercase
  }

  /**
   * Store user playback speed selection in preference
   *
   * @param playbackSpeed playback speed
   */
  fun updateSelectedPlaybackSpeed(playbackSpeed: Float) {
    settingsPrefs.savePlaybackSpeed(playbackSpeed)
  }

  /**
   * Store user playback quality selection in preference
   *
   * @param quality playback quality
   */
  fun updateSelectedPlaybackQuality(quality: Int) {
    settingsPrefs.savePlaybackQuality(quality)
  }

  /**
   * Get stored playback speed
   *
   * @return playback speed
   */
  fun getPlaybackSpeed(): Float = settingsPrefs.getPlaybackSpeed()

  /**
   * Get stored playback quality
   *
   * @return playback quality
   */
  fun getPlaybackQuality(): Int = settingsPrefs.getPlaybackQuality()

  /**
   * Get stored subtitle language
   *
   * @return subtitle language
   */
  fun getSubtitleLanguage(): String = settingsPrefs.getSubtitleLanguage()

  /**
   * Get auto play next
   *
   * @return true, if user has allowed auto playback, else False
   */
  fun getAutoPlayNextAllowed(): Boolean = settingsPrefs.getAutoPlayAllowed()

}
