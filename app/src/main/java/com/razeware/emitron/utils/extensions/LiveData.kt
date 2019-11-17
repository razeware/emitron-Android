package com.razeware.emitron.utils.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer


/**
 * Extension to run block inside [Observer]
 */
fun <T> LiveData<T>.observe(owner: LifecycleOwner, block: (T?) -> Unit): Unit =
  observe(owner, Observer { block(it) })
