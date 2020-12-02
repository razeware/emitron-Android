package com.raywenderlich.android.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

/**
 * Helper class to manage app preferences
 */
class PrefUtils(private val context: Context) {

  /**
   * Create and return a custom preferences
   *
   * @param name name of shared preference file
   *
   * @return SharedPreferences
   */
  fun getSharedPrefs(name: String) = context.getSharedPreferences(name, Context.MODE_PRIVATE)

  /**
   * Return default shared preferences
   */
  fun getSharedPrefs() = PreferenceManager.getDefaultSharedPreferences(context)

  private lateinit var prefs: SharedPreferences
  private val editor: SharedPreferences.Editor by lazy(LazyThreadSafetyMode.NONE) { prefs.edit() }

  /**
   * Initialise [SharedPreferences] object for read/write
   */
  fun init(name: String) {
    prefs = getSharedPrefs(name)
  }

  /**
   * Set a value to preference.
   *
   * Function will use type of value to decide how the preference will be stored.
   *
   * Don't forget to call [PrefUtils.commit()]
   *
   * @param key key for Preference
   * @param value value for preference
   *
   * @return Instance of Pre
   */
  fun <T> set(key: String, value: T): PrefUtils {
    when (value) {
      is Boolean -> editor.putBoolean(key, value)
      is String -> editor.putString(key, value)
      is Int -> editor.putInt(key, value)
      is Float -> editor.putFloat(key, value)
      else -> throw IllegalStateException()
    }
    return this
  }

  /**
   * Save preferences to file
   */
  fun commit(): Boolean = editor.commit()

  /**
   * Clear all preferences
   */
  fun clear(): Boolean = prefs.edit().clear().commit()

  /**
   * Get a specific preference value by key
   *
   * @param key key for Preference
   * @param defaultVal default value for preference
   *
   * @return stored value of the preference if it exists or default value
   */
  @Suppress("UNCHECKED_CAST")
  fun <T> get(key: String, defaultVal: T): T {
    return when (defaultVal) {
      is Boolean -> prefs.getBoolean(key, defaultVal) as T
      is String -> prefs.getString(key, defaultVal) as T
      is Int -> prefs.getInt(key, defaultVal) as T
      is Float -> prefs.getFloat(key, defaultVal) as T
      else -> throw IllegalStateException()
    }
  }

}
