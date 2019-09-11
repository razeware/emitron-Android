package com.raywenderlich.emitron.data.settings

import androidx.appcompat.app.AppCompatDelegate
import com.raywenderlich.emitron.data.prefs.PrefUtils
import javax.inject.Inject

/**
 * Prefs helper for user settings
 *
 * Data related to user settings preferences should be stored and accessed using [SettingsPrefs]
 */
class SettingsPrefs @Inject constructor(private val prefs: PrefUtils) {

  companion object {
    private const val RECENT_SEARCHES = "recent_searches"
    private const val ALLOW_CRASH_REPORTS = "allow_crash_reports"
    private const val SELECTED_NIGHT_MODE = "selected_night_mode"
  }

  init {
    prefs.init("settings")
  }

  /**
   * Store the recent search query
   *
   * @param query Search term
   */
  fun saveSearchQuery(query: String) {
    val recentSearchTerms = getSearchQueries().toMutableList()
    if (recentSearchTerms.contains(query)) {
      recentSearchTerms.remove(query)
      recentSearchTerms.add(0, query)
    } else {
      recentSearchTerms.add(0, query)
    }
    val lastFiveSearchTerms = if (recentSearchTerms.size >= 5) {
      recentSearchTerms.take(5)
    } else {
      recentSearchTerms
    }
    with(prefs) {
      set(
        RECENT_SEARCHES, lastFiveSearchTerms.toString()
          .replace("[", "")
          .replace("]", "")
      )
      commit()
    }
  }

  /**
   * Get recently searched queries
   *
   */
  fun getSearchQueries(): List<String> {
    val prefs = prefs.get(RECENT_SEARCHES, "")
    return if (prefs.isEmpty()) {
      emptyList()
    } else {
      prefs.split(",").map { it.trim() }
    }
  }

  /**
   * Clear preferences
   */
  fun clear() {
    prefs.clear()
  }

  /**
   * Store crash reporting allowed by user in preference
   *
   * @param allowed True if user has allowed to store crash reporting false otherwise.
   */
  fun saveCrashReportingAllowed(allowed: Boolean) {
    prefs.set(ALLOW_CRASH_REPORTS, allowed).commit()
  }

  /**
   * Store user night mode choice in preference
   *
   * @param nightMode Current bight mode setting
   */
  fun saveNightMode(nightMode: Int) {
    prefs.set(SELECTED_NIGHT_MODE, nightMode).commit()
  }

  /**
   * Get if user allowed to report crashes
   *
   * @return True, if user has allowed crash reporting, else False
   */
  fun isCrashReportingAllowed(): Boolean =
    prefs.get(ALLOW_CRASH_REPORTS, false)


  /**
   * Get user stored night mode from preferences
   *
   * @return current night mode
   */
  fun getNightMode(): Int =
    prefs.get(SELECTED_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_YES)
}
