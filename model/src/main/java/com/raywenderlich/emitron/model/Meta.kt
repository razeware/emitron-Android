package com.raywenderlich.emitron.model

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Meta(
  @Json(name = "total_result_count")
  var totalResultCount: Int = 0,
  var count: Int = 0
) : Parcelable
