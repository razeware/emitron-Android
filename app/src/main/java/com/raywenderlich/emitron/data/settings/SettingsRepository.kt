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
   * @param nightMode Current bight mode setting
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
}
