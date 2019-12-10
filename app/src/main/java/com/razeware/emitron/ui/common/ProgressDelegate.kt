package com.razeware.emitron.ui.common

import android.view.View
import com.razeware.emitron.R

/**
 * Helper class to manage visibility of progress view
 */
class ProgressDelegate(private val view: View) {

  /**
   * Call to show progress view
   */
  fun showProgressView() {
    view.findViewById<View>(R.id.layout_progress_container).visibility = View.VISIBLE
    view.findViewById<View>(R.id.search_view_library_click_blocker)?.visibility = View.VISIBLE
  }

  /**
   * Call to hide progress view
   */
  fun hideProgressView() {
    view.findViewById<View>(R.id.layout_progress_container).visibility = View.GONE
    view.findViewById<View>(R.id.search_view_library_click_blocker)?.visibility = View.GONE
  }
}
