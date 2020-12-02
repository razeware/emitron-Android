package com.raywenderlich.android.inappreview.preferences

/**
 * Provides the preferences that store if the user reviewed the app already, or not.
 *
 * It also stores info if the user chose to review the app later or never.
 *
 * If the user chose to never review the app - we don't prompt them with the review flow or dialog.
 * If the user chose to review the app later - we check if enough time has passed (e.g. a week).
 * If the user already chose to review the app, we shouldn't show the dialog or prompt again.
 * */
interface InAppReviewPreferences {

  /**
   * @return If the user has chosen the "Rate App" option or not.
   * */
  fun hasUserRatedApp(): Boolean

  /**
   * Stores if the user chose to rate the app or not.
   *
   * @param hasRated - If the user chose the "Rate App" option.
   * */
  fun setUserRatedApp(hasRated: Boolean)

  /**
   * @return If the user has chosen the "Ask Me Later" option or not.
   * */
  fun hasUserChosenRateLater(): Boolean

  /**
   * Stores if the user wants to rate the app later.
   *
   * @param hasChosenRateLater - If the user chose the "Ask Me Later" option.
   * */
  fun setUserChosenRateLater(hasChosenRateLater: Boolean)

  /**
   * @return Timestamp when the user chose the "Ask Me Later" option.
   * */
  fun getRateLaterTime(): Long

  /**
   * Stores the time when the user chose to review the app later.
   *
   * @param time - The timestamp when the user chose the "Ask Me Later" option.
   * */
  fun setRateLater(time: Long)

  /**
   * Clears out the preferences.
   *
   * Useful for situations where we add some crucial features to the app and want to ask the user
   * for an opinion.
   *
   * This should be used only if the user didn't rate the app before.
   * */
  fun clearIfUserDidNotRate()
}