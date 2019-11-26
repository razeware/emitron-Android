package com.razeware.emitron.model

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class Meta(
  @Json(name = "total_result_count")
  var totalResultCount: Int = 0,
  var count: Int = 0
) : Parcelable
