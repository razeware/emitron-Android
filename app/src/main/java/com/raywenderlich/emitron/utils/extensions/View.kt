package com.raywenderlich.emitron.utils.extensions

import android.view.View


/**
 * Return visibility based on constraint
 *
 * @param constraint True/False
 *
 * @return [View.VISIBLE] if constraint is true, else [View.GONE]
 */
fun View.toVisibility(constraint: Boolean) {
  visibility = if (constraint) {
    View.VISIBLE
  } else {
    View.GONE
  }
}
