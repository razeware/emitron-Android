package com.raywenderlich.emitron.model

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

/**
 * Model for Bookmark, Content response
 */
@JsonClass(generateAdapter = true)
@Parcelize
data class Content(
  @Json(name = "data")
  var datum: Data? = null,
  val links: Links? = null,
  val meta: Meta? = null,
  val included: List<Data>? = null
) : Parcelable {

  var hasSubscription = false

}