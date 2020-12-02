package com.razeware.emitron.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.razeware.emitron.data.login.LoginRepository
import com.razeware.emitron.data.settings.SettingsRepository

/**
 * ViewModel for settings view
 */
class SettingsViewModel @ViewModelInject constructor(
  private val loginRepository: LoginRepository,
  private val settingsRepository: SettingsRepository
) : ViewModel() {

  private val _nightMode = MutableLiveData<Int>()

  private val _crashReportingAllowed = MutableLiveData<Boolean>()

  private val _playbackSpeed = MutableLiveData<Float>()

  private val _playbackQuality = MutableLiveData<Int>()

  private val _subtitlesLanguage = MutableLiveData<String>()

  private val _downloadQuality = MutableLiveData<String>()

  private val _downloadsWifiOnly = MutableLiveData<Boolean>()

  init {
    init()
  }

  /**
   * Init with default values
   */
  fun init() {
    _nightMode.value = settingsRepository.getNightMode()
    _crashReportingAllowed.value = settingsRepository.isCrashReportingAllowed()
    _playbackSpeed.value = settingsRepository.getPlaybackSpeed()
    _playbackQuality.value = settingsRepository.getPlaybackQuality()
    _subtitlesLanguage.value = settingsRepository.getSubtitleLanguage()
    _downloadQuality.value = settingsRepository.getDownloadQuality()
    _downloadsWifiOnly.value = settingsRepository.getDownloadsWifiOnly()
  }

  /**
   * Observer for night mode setting
   */
  val nightMode: LiveData<Int>
    get() = _nightMode

  /**
   * Observer for crash reporting setting
   */
  val crashReportingAllowed: LiveData<Boolean>
    get() = _crashReportingAllowed

  /**
   * Observer for playback speed
   */
  val playbackSpeed: LiveData<Float>
    get() = _playbackSpeed

  /**
   * Observer for playback quality
   */
  val playbackQuality: LiveData<Int>
    get() = _playbackQuality

  /**
   * Observer for subtitle language
   */
  val subtitlesLanguage: LiveData<String>
    get() = _subtitlesLanguage

  /**
   * Observer for subtitle language
   */
  val downloadQuality: LiveData<String>
    get() = _downloadQuality

  /**
   * Observer for subtitle language
   */
  val downloadsWifiOnly: LiveData<Boolean>
    get() = _downloadsWifiOnly


  /**
   * Logout user
   */
  fun logout() {
    loginRepository.deleteUser()
  }

  /**
   * Update crash reporting allowed in preferences
   *
   * @param allowed True if crash reporting is allowed, else False
   */
  fun updateCrashReportingAllowed(allowed: Boolean) {
    settingsRepository.updateCrashReportingAllowed(allowed)
  }

  /**
   * Update selected night mode in preferences
   *
   * @param nightMode Either of [AppCompatDelegate.MODE_NIGHT_YES],
   * [AppCompatDelegate.MODE_NIGHT_NO], [AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM]
   */
  fun updateNightMode(nightMode: Int) {
    settingsRepository.updateNightMode(nightMode)
    _nightMode.value = nightMode
  }

  /**
   * Update selected playback speed in preferences
   *
   * @param speed Playback speed
   */
  fun updatePlaybackSpeed(speed: Float) {
    settingsRepository.updateSelectedPlaybackSpeed(speed)
    _playbackSpeed.value = speed
  }

  /**
   * Update selected playback quality in preferences
   *
   * @param quality Playback quality
   */
  fun updatePlaybackQuality(quality: Int) {
    settingsRepository.updateSelectedPlaybackQuality(quality)
    _playbackQuality.value = quality
  }

  /**
   * Update selected subtitle language in preferences
   *
   * @param language Subtitle language
   */
  fun updateSubtitlesLanguage(language: String) {
    settingsRepository.updateSubtitleLanguage(language)
    _subtitlesLanguage.value = language
  }

  /**
   * Update selected download quality in preferences
   *
   * @param quality download quality (hd/sd)
   */
  fun updateDownloadQuality(quality: String) {
    settingsRepository.updateSelectedDownloadQuality(quality)
    _downloadQuality.value = quality
  }

  /**
   * Update crash reporting allowed in preferences
   *
   * @param wifiOnly true if downloads allowed only on wifi else false
   */
  fun updateDownloadsWifiOnly(wifiOnly: Boolean) {
    settingsRepository.updateDownloadsWifiOnly(wifiOnly)
  }

  /**
   * Get saved night mode
   */
  fun getNightMode(): Int = settingsRepository.getNightMode()

  /**
   * Get saved playback quality
   */
  fun getPlaybackQuality(): Int = settingsRepository.getPlaybackQuality()

  /**
   * Get saved playback speed
   */
  fun getPlaybackSpeed(): Float = settingsRepository.getPlaybackSpeed()

  /**
   * Get saved subtitle language
   */
  fun getSubtitleLanguage(): String = settingsRepository.getSubtitleLanguage()

  /**
   * Get download quality from preference
   */
  fun getDownloadQuality(): String = settingsRepository.getDownloadQuality()

  /**
   * Get download wifi only
   */
  fun getDownloadsWifiOnly(): Boolean = settingsRepository.getDownloadsWifiOnly()

  /**
   * Get logged in user
   */
  fun getLoggedInUser(): String = settingsRepository.getLoggedInUser()
}
