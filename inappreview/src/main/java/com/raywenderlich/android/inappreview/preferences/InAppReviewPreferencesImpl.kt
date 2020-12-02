package com.raywenderlich.android.inappreview.preferences

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject

/**
 * Provides the preferences that store if the user rated the app already, or not.
 *
 * It also stores info if the user chose to rate the app later.
 *
 * If the user chose to rate the app later - we check if enough time has passed (e.g. a week).
 * If the user already chose to rate the app, we shouldn't show the dialog or prompt again.
 * */
class InAppReviewPreferencesImpl @Inject constructor(
  private val sharedPreferences: SharedPreferences
) : InAppReviewPreferences {

  companion object {
    private const val KEY_HAS_RATED_APP = "hasRatedApp"
    private const val KEY_CHOSEN_RATE_LATER = "rateLater"
    private const val KEY_RATE_LATER_TIME = "rateLaterTime"
  }

  /**
   * @return If the user has chosen the "Rate App" option or not.
   * */
  override fun hasUserRatedApp(): Boolean =
    sharedPreferences.getBoolean(KEY_HAS_RATED_APP, false)

  /**
   * Stores if the user chose to rate the app or not.
   *
   * @param hasRated - If the user chose the "Rate App" option.
   * */
  override fun setUserRatedApp(hasRated: Boolean) =
    sharedPreferences.edit { putBoolean(KEY_HAS_RATED_APP, hasRated) }

  /**
   * @return If the user has chosen the "Ask Me Later" option or not.
   * */
  override fun hasUserChosenRateLater(): Boolean =
    sharedPreferences.getBoolean(KEY_CHOSEN_RATE_LATER, false)

  /**
   * Stores if the user wants to rate the app later.
   *
   * @param hasChosenRateLater - If the user chose the "Ask Me Later" option.
   * */
  override fun setUserChosenRateLater(hasChosenRateLater: Boolean) =
    sharedPreferences.edit { putBoolean(KEY_CHOSEN_RATE_LATER, hasChosenRateLater) }

  /**
   * @return Timestamp when the user chose the "Ask Me Later" option.
   * */
  override fun getRateLaterTime(): Long =
    sharedPreferences.getLong(KEY_RATE_LATER_TIME, System.currentTimeMillis())

  /**
   * Stores the time when the user chose to review the app later.
   *
   * @param time - The timestamp when the user chose the "Ask Me Later" option.
   * */
  override fun setRateLater(time: Long) =
    sharedPreferences.edit { putLong(KEY_RATE_LATER_TIME, time) }

  /**
   * Clears out the preferences.
   *
   * Useful for situations where we add some crucial features to the app and want to ask the user
   * for an opinion.
   *
   * This should be used only if the user didn't rate the app before. E.g. the user chose to rate
   * "later" and we add a new feature where you can preview books in the app.
   *
   * This is a big change and even though the user chose not to give a rating before, they might be
   * convinced to rate the app after they've tried out the new features.
   * */
  override fun clearIfUserDidNotRate() {
    if (!hasUserRatedApp()) {
      sharedPreferences.edit {
        putBoolean(KEY_CHOSEN_RATE_LATER, false)
        putLong(KEY_RATE_LATER_TIME, 0)
      }
    }
  }
}