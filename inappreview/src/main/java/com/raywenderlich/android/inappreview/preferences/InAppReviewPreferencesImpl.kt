package com.raywenderlich.android.inappreview.preferences

import android.content.SharedPreferences

/**
 * Provides the preferences that store if the user rated the app already, or not.
 *
 * It also stores info if the user chose to rate the app later or never.
 *
 * If the user chose to never rate the app - we don't prompt them with the review flow or dialog.
 * If the user chose to rate the app later - we check if enough time has passed (e.g. a week).
 * If the user already chose to rate the app, we shouldn't show the dialog or prompt again.
 * */
class InAppReviewPreferencesImpl(
  private val sharedPreferences: SharedPreferences
) : InAppReviewPreferences {

  companion object {
    private const val KEY_HAS_RATED_APP = "hasRatedApp"
    private const val KEY_CHOSEN_RATE_LATER = "rateLater"
    private const val KEY_CHOSEN_RATE_NEVER = "rateNever"
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
   * @param hasReviewed - If the user chose the "Rate App" option.
   * */
  override fun saveUserRatedApp(hasReviewed: Boolean) =
    sharedPreferences.edit()
      .putBoolean(KEY_HAS_RATED_APP, hasReviewed)
      .apply()

  /**
   * @return If the user has chosen the "Ask Me Later" option or not.
   * */
  override fun hasUserChosenRateLater(): Boolean =
    sharedPreferences.getBoolean(KEY_CHOSEN_RATE_LATER, false)

  /**
   * Stores if the user wants to rate the app later.
   *
   * @param hasChosenReviewLater - If the user chose the "Ask Me Later" option.
   * */
  override fun setUserChosenRateLater(hasChosenReviewLater: Boolean) =
    sharedPreferences.edit()
      .putBoolean(KEY_CHOSEN_RATE_LATER, hasChosenReviewLater)
      .apply()

  /**
   * @return If the user has chosen the "Don't Ask Me Again" option or not.
   * */
  override fun hasUserChosenRateNever(): Boolean =
    sharedPreferences.getBoolean(KEY_CHOSEN_RATE_NEVER, false)

  /**
   * Stores if the user doesn't want to rate the app.
   *
   * @param hasChosenReviewNever - If the user chose the "Don't Ask Me Again" option.
   * */
  override fun setUserChosenRateNever(hasChosenReviewNever: Boolean) =
    sharedPreferences.edit()
      .putBoolean(KEY_CHOSEN_RATE_NEVER, hasChosenReviewNever)
      .apply()

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
    sharedPreferences.edit()
      .putLong(KEY_RATE_LATER_TIME, time)
      .apply()

  /**
   * Clears out the preferences.
   *
   * Useful for situations where we add some crucial features to the app and want to ask the user
   * for an opinion.
   *
   * This should be used only if the user didn't rate the app before. E.g. the user chose to rate
   * "later" or "never" and we add a new feature where you can preview books in the app.
   *
   * This is a big change and even though the user chose not to give a rating before, they might be
   * convinced to rate the app after they've tried out the new features.
   * */
  override fun clearIfUserDidNotRate() {
    if (!hasUserRatedApp()) {
      sharedPreferences.edit()
        .putBoolean(KEY_CHOSEN_RATE_NEVER, false)
        .putBoolean(KEY_CHOSEN_RATE_LATER, false)
        .putLong(KEY_RATE_LATER_TIME, 0)
        .apply()
    }
  }
}