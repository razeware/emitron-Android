package com.raywenderlich.android.inappreview

/**
 * Represents an interface all features that can prompt the In App Review Flow should implement.
 * */
interface InAppReviewView {

  /**
   * Tells the UI to attempt to trigger the In App Review flow.
   * This doesn't guarantee the Flow will be triggered, as Google imposes hidden, mutable, quotas
   * for review flow requests.
   * */
  fun showReviewFlow()
}