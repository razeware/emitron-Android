package com.razeware.emitron.utils

import androidx.annotation.MainThread
import androidx.paging.PagedList
import javax.inject.Inject

/**
 * Helper to pass [PagedList.BoundaryCallback] state of
 * - running parallel update operations count
 * - when it should reset page number
 *
 */
class BoundaryCallbackNotifier @Inject constructor() {
  internal var requestCount = 0
  internal var pageReset = false
}

/**
 * Increment running parallel content job count
 */
@MainThread
fun BoundaryCallbackNotifier?.increment() {
  val currentValue = this?.requestCount ?: 0
  this?.requestCount = currentValue + 1
}

/**
 * Decrement running parallel content job count, Also update [BoundaryCallbackNotifier.reset]
 * to True so that Boundary callback can reset it's page no.
 */
@MainThread
fun BoundaryCallbackNotifier?.decrement() {
  val currentValue = this?.requestCount ?: 0
  if (currentValue != 0) {
    this?.requestCount = currentValue - 1
  }
  reset(true)
}

/**
 * Set if boundary callback should reset it's page number
 */
@MainThread
fun BoundaryCallbackNotifier?.reset(boolean: Boolean) {
  this?.pageReset = boolean
}

/**
 * @return True if running request count is 1 or more, else false
 */
@MainThread
fun BoundaryCallbackNotifier?.hasRequests(): Boolean {
  if (this == null) return false

  return this.requestCount > 0
}

/**
 *
 * @return True if boundary callback should reset it's page number
 */
@MainThread
fun BoundaryCallbackNotifier?.shouldReset(): Boolean {
  if (this == null) return false

  val shouldReset = this.pageReset
  if (shouldReset) {
    reset(false)
  }
  return shouldReset
}
