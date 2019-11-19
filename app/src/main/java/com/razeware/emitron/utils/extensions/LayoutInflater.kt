package com.razeware.emitron.utils.extensions

import android.view.LayoutInflater
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding


/**
 * Extension function to inflate a databinding layout from a [LayoutInflater]
 *
 * Call this function using a [LayoutInflater] instance to get [ViewDataBinding]
 *
 * @param layoutId Int Layout resource id
 *
 * @return ViewDataBinding binding for layout resource
 */
fun <T : ViewDataBinding> LayoutInflater.inflateDatabindingLayout(
  @LayoutRes
  layoutId: Int
): T =
  (DataBindingUtil.inflate(
    this,
    layoutId,
    null,
    false
  ) as T)
