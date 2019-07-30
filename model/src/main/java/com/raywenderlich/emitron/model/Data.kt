package com.raywenderlich.emitron.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 *  Model class for Bookmarks, Domains, Progressions, Contents, Groups.
 */
@Parcelize
data class Data(
  /**
   *  Id
   */
  val id: String? = null,
  /**
   * Maps to [DataType]
   */
  val type: String? = null,
  /**
   *  Attributes
   */
  val attributes: Attributes? = null,
  /**
   *  Links
   */
  val links: Links? = null,
  /**
   *  Relationships
   */
  val relationships: Relationships? = Relationships(),
  /**
   *  Meta
   */
  val meta: Meta? = null,
  /**
   *  Contents
   */
  val included: Contents? = null
) : Parcelable
