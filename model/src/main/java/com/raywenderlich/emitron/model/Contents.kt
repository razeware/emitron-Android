package com.raywenderlich.emitron.model

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

/**
 * Model for list response of Bookmark, Content, Progressions
 */
@Parcelize
data class Contents(
  /**
   * List of content
   */
  @Json(name = "data")
  val datum: List<Data> = emptyList(),

  /**
   * Related content referred in list of contents
   */
  val included: List<Data>? = null,

  /**
   * Related links
   */
  val links: Links? = null,

  /**
   * Related meta data
   */
  val meta: Meta? = null
) : Parcelable
