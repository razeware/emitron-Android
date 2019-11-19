package com.razeware.emitron.utils.extensions

import android.widget.RadioGroup

/**
 * Extension to convert Int value to pixels
 */
fun RadioGroup.getCheckedChildPositionById(checkedId: Int): Int =
  indexOfChild(findViewById(checkedId))

fun RadioGroup.setCheckedChildByPosition(checkedPosition: Int): Unit =
  check(getChildAt(checkedPosition).id)
