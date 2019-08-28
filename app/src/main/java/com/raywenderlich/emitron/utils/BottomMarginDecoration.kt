package com.raywenderlich.emitron.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.emitron.R

/**
 * Margin decoration for list view items
 */
class BottomMarginDecoration : RecyclerView.ItemDecoration() {

  /**
   * Override item offsets to add bottom margin
   */
  override fun getItemOffsets(
    outRect: Rect,
    view: View,
    parent: RecyclerView,
    state: RecyclerView.State
  ) {
    outRect.bottom =
      view.context.resources.getDimensionPixelSize(R.dimen.activity_vertical_margin)
  }
}
