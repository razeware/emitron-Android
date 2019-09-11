package com.raywenderlich.emitron.model

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

  fun isCompleted(): Boolean = this == Completed

  companion object {

    /**
     * Map of all [CompletionStatus]
     */
    internal val map = values().associateBy(CompletionStatus::name)
  }
}
