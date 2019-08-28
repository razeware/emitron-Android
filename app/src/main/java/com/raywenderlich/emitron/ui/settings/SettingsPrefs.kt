package com.raywenderlich.emitron.ui.settings

import androidx.collection.ArraySet
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
    val recentSearchTerms = getSearchQueries()
    val updatedRecentSearchTerms = ArraySet(recentSearchTerms).toMutableList()
    if (recentSearchTerms.contains(query)) {
      updatedRecentSearchTerms.remove(query)
      updatedRecentSearchTerms.add(0, query)
    } else {
      updatedRecentSearchTerms.add(0, query)
    }
    val lastFiveSearchTerms = if (updatedRecentSearchTerms.size >= 5) {
      updatedRecentSearchTerms.take(5)
    } else {
      updatedRecentSearchTerms
    }
    with(prefs) {
      set(RECENT_SEARCHES, lastFiveSearchTerms.toSet())
    }
  }

  /**
   * Get recently searched queries
   *
   */
  fun getSearchQueries(): Set<String> = prefs.get(RECENT_SEARCHES, emptySet())

  /**
   * Clear preferences
   */
  fun clear() {
    prefs.clear()
  }
}
