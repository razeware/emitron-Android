package com.razeware.emitron.ui.common

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.razeware.emitron.R

/**
 * Item decoration for my tutorials items
 */
class StartEndBottomMarginDecoration : RecyclerView.ItemDecoration() {


  /**
   * Update item offsets to add bottom, left, right margin
   *
   * See [RecyclerView.ItemDecoration.getItemOffsets]
   */
  override fun getItemOffsets(
    outRect: Rect,
    view: View,
    parent: RecyclerView,
    state: RecyclerView.State
  ) {
    outRect.bottom = view.context.resources.getDimensionPixelSize(R.dimen.activity_vertical_margin)
    outRect.left = view.context.resources.getDimensionPixelSize(R.dimen.activity_vertical_margin)
    outRect.right = view.context.resources.getDimensionPixelSize(R.dimen.activity_vertical_margin)
  }
}
