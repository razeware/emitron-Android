package com.razeware.emitron.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Content completion status
 *
 * @param param Value to be use in http requests
 */
@Parcelize
enum class CompletionStatus(val param: String) : Parcelable {

  /**
   * Progress in progress
   */
  InProgress("in_progress"),

  /**
   * Progress is completed
   */
  Completed("completed");

  companion object {

    /**
     * Map of all [CompletionStatus]
     */
    internal val map = values().associateBy(CompletionStatus::name)
  }
}


/**
 * Check is completion status is [CompletionStatus.Completed]
 */
fun CompletionStatus?.isCompleted(): Boolean = this == CompletionStatus.Completed
