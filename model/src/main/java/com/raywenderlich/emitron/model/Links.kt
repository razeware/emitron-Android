package com.raywenderlich.emitron.model

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Links(
  val first: String? = "",
  val last: String? = "",
  val next: String? = "",
  val prev: String? = "",
  val self: String = "",
  @Json(name = "video_stream")
  val videoStream: String = "",
  @Json(name = "video_download")
  val videoDownload: String = ""
) : Parcelable
