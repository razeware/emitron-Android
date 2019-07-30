package com.raywenderlich.emitron.model

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

/**
 * Relationships
 */
@Parcelize
data class Relationships(
  /**
   * Related content
   */
  val content: Content? = null,

  /**
   * Related contents
   */
  val contents: Contents? = null,

  /**
   * Related bookmark
   */
  val bookmark: Content? = null,

  /**
   * Related domains
   */
  val domains: Contents? = null,

  /**
   * Related progression
   */
  val progression: Content? = null,

  /**
   * Related groups
   */
  val groups: Contents? = null,

  /**
   * Related child contents
   */
  @Json(name = "child_contents")
  val childContents: Content? = null
) : Parcelable
