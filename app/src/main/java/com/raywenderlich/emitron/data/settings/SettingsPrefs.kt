package com.raywenderlich.emitron.data.settings

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
}
