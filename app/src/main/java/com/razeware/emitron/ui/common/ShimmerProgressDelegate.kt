package com.razeware.emitron.ui.common

import android.graphics.drawable.AnimationDrawable
import android.view.View
import com.razeware.emitron.R

/**
 * Helper class to show/hide shimmer progress animation
 *
 * Ensure `R.layout.layout_collection_shimmer_progress` is added to the layout file,
 * in a container view with id layout_shimmer_progress_container
 */
class ShimmerProgressDelegate(private val view: View) {

  private fun getProgressViews() = arrayOf<View>(
    view.findViewById(R.id.layout_progress_item_1),
    view.findViewById(R.id.layout_progress_item_2)
  )

  /**
   * Call to show progress view
   */
  fun showProgressView() {
    view.findViewById<View>(R.id.layout_shimmer_progress_container).visibility = View.VISIBLE
    getProgressViews().map { view ->
      (view.background as? AnimationDrawable)?.let { drawable ->
        drawable.setEnterFadeDuration(500)
        drawable.setExitFadeDuration(500)
        drawable.start()
      }
    }
  }

  /**
   * Call to hide progress view
   */
  fun hideProgressView() {
    view.findViewById<View>(R.id.layout_shimmer_progress_container).visibility = View.GONE
    getProgressViews().map { view ->
      (view.background as? AnimationDrawable)?.stop()
    }
  }
}
