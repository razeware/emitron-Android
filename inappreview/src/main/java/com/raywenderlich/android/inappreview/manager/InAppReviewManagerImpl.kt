package com.raywenderlich.android.inappreview.manager

import android.app.Activity
import android.util.Log
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.raywenderlich.android.inappreview.BuildConfig
import com.raywenderlich.android.inappreview.preferences.InAppReviewPreferences
import java.util.concurrent.TimeUnit

/**
 * The review manager implementation wrapper, that starts and handles the In-App Review
 * flow.
 *
 * @param reviewManager - The [ReviewManager] that handles all the internal API calls.
 * @property reviewInfo - The info for the app that enables In-App Review calls.
 * */
class InAppReviewManagerImpl(
  private val reviewManager: ReviewManager,
  private val inAppReviewPreferences: InAppReviewPreferences
) : InAppReviewManager {

  companion object {
    private const val KEY_REVIEW = "reviewFlow"
  }

  private var reviewInfo: ReviewInfo? = null

  /**
   * After the class is created, we request the [ReviewInfo] to pre-cache it if the user is eligible.
   * The [ReviewInfo] is used to request the review flow later in the app.
   * */
  init {
    if (isEligibleForReview()) {
      reviewManager.requestReviewFlow().addOnCompleteListener {
        if (it.isComplete && it.isSuccessful) {
          this.reviewInfo = it.result
        }
      }
    }
  }

  /**
   * Returns if the user is eligible for a review.
   *
   * They are eligible only if they haven't rated before and they haven't chosen the "never" option,
   * or if they asked to rate later and a week has passed.
   * */
  override fun isEligibleForReview(): Boolean {
    return (!inAppReviewPreferences.hasUserRatedApp()
        && !inAppReviewPreferences.hasUserChosenRateNever())
        || (inAppReviewPreferences.hasUserChosenRateLater() && enoughTimePassed())
  }

  private fun enoughTimePassed(): Boolean {
    val rateLaterTimestamp = inAppReviewPreferences.getRateLaterTime()

    return System.currentTimeMillis() - rateLaterTimestamp >= TimeUnit.DAYS.toMillis(7)
  }

  /**
   * Attempts to start review flow if the [reviewInfo] is available and the user is eligible.
   *
   * @param activity - The Activity to which the lifecycle is attached.
   * */
  override fun startReview(activity: Activity) {
    if (reviewInfo != null && isEligibleForReview()) {
      reviewManager.launchReviewFlow(activity, reviewInfo).addOnCompleteListener {
        if (BuildConfig.DEBUG && it.isComplete && it.isSuccessful) {
          Log.d(KEY_REVIEW, "Review complete: ${it.isComplete}, successful: ${it.isSuccessful}")
        }
      }
    }
  }
}