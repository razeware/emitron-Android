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
    private const val PLAYER_AUTO_PLAY_NEXT = "player_auto_play_next"
    private const val PLAYER_PLAYBACK_SPEED = "player_playback_speed"
    private const val PLAYER_PLAYBACK_QUALITY = "player_playback_quality"
    private const val PLAYER_SUBTITLES_LANGUAGE = "player_subtitles_language"
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

  /**
   * Save auto play preference
   *
   * @param allowed true if user has allowed auto play else false
   */
  fun saveAutoPlayAllowed(allowed: Boolean) {
    prefs.set(PLAYER_AUTO_PLAY_NEXT, allowed).commit()
  }

  /**
   * Save playback speed
   *
   * @param speed playback speed
   */
  fun savePlaybackSpeed(speed: Float) {
    prefs.set(PLAYER_PLAYBACK_SPEED, speed).commit()
  }

  /**
   * Save playback quality
   *
   * @param quality playback quality
   */
  fun savePlaybackQuality(quality: Int) {
    prefs.set(PLAYER_PLAYBACK_QUALITY, quality).commit()
  }

  /**
   * Save subtitle language
   *
   * @param language subtitle language
   */
  fun saveSubtitleLanguage(language: String) {
    prefs.set(PLAYER_SUBTITLES_LANGUAGE, language).commit()
  }

  /**
   * Get playback speed from preference
   *
   * @return playback speed
   */
  fun getPlaybackSpeed(): Float =
    prefs.get(PLAYER_PLAYBACK_SPEED, 1f)

  /**
   * Get playback speed from preference
   *
   * @return playback quality
   */
  fun getPlaybackQuality(): Int =
    prefs.get(PLAYER_PLAYBACK_QUALITY, 1080)

  /**
   * Get playback speed from preference
   *
   * @return auto play allowed, true if user has allowed auto play else false
   */
  fun getAutoPlayAllowed(): Boolean =
    prefs.get(PLAYER_AUTO_PLAY_NEXT, true)

  /**
   * Get subtitle language from preference
   *
   * @return subtitle language in ISO format ex. "en"
   */
  fun getSubtitleLanguage(): String =
    prefs.get(PLAYER_SUBTITLES_LANGUAGE, "")
}
