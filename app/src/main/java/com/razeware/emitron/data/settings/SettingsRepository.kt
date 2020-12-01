package com.razeware.emitron.data.settings

import androidx.annotation.WorkerThread
import com.raywenderlich.android.preferences.GeneralSettingsPrefs
import com.razeware.emitron.data.content.ContentDataSourceLocal
import com.razeware.emitron.data.login.LoginPrefs
import com.razeware.emitron.data.progressions.ProgressionDataSourceLocal
import com.razeware.emitron.ui.onboarding.OnboardingView
import com.razeware.emitron.utils.async.ThreadManager
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Repository for settings data
 */
class SettingsRepository @Inject constructor(
  private val threadManager: ThreadManager,
  private val loginPrefs: LoginPrefs,
  private val settingsPrefs: GeneralSettingsPrefs,
  private val contentDataSourceLocal: ContentDataSourceLocal,
  private val progressionDataSourceLocal: ProgressionDataSourceLocal
) {

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

  /**
   * Get download quality from preference
   *
   * @return download quality
   */
  fun getDownloadQuality(): String = settingsPrefs.getDownloadQuality()

  /**
   * Get downloads wifi only from preference
   *
   * @return true if download allowed only on wifi else false
   */
  fun getDownloadsWifiOnly(): Boolean = settingsPrefs.getDownloadsWifiOnly()

  /**
   * Store user download quality selection in preference
   *
   * @param quality download quality (hd/sd)
   */
  fun updateSelectedDownloadQuality(quality: String) {
    settingsPrefs.saveDownloadQuality(quality)
  }

  /**
   * Store user download network preference
   *
   * @param wifiOnly true if downloads allowed only on wifi else false
   */
  fun updateDownloadsWifiOnly(wifiOnly: Boolean) {
    settingsPrefs.saveDownloadsWifiOnly(wifiOnly)
  }

  /**
   * Logout
   */
  @WorkerThread
  suspend fun logout() {
    withContext(threadManager.db) {
      contentDataSourceLocal.deleteAll()
      progressionDataSourceLocal.deleteWatchStats()
    }
    settingsPrefs.clear()
  }

  /**
   * Get if user wants to see onboarding views
   *
   * @return True, if user has allowed onboarding views, else False
   */
  fun isOnboardingAllowed(): Boolean {
    return false
  }

  /**
   * Update if user allowed onboarding views
   *
   */
  fun updateOnboardingAllowed(allowed: Boolean) {
    settingsPrefs.saveOnboardingAllowed(allowed)
  }

  /**
   * Save onboarded view
   *
   * @param view onboarded view [OnboardingView]
   */
  fun updateOnboardedView(view: OnboardingView) {
    settingsPrefs.saveOnboardedView(view.toString())
  }

  /**
   * Get all onboarded views
   *
   * @return list of onboarded views [OnboardingView]
   */
  fun getOnboardedViews(): List<OnboardingView> =
    settingsPrefs.getOnboardedViews().mapNotNull {
      if (it.isNotEmpty()) {
        OnboardingView.valueOf(it)
      } else {
        null
      }
    }

  /**
   * Get logged in user
   *
   * @return String logged in user
   */
  fun getLoggedInUser(): String = loginPrefs.getLoggedInUser()
}
