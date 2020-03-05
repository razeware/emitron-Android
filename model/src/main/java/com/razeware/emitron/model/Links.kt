package com.razeware.emitron.model

import android.net.Uri
import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@JsonClass(generateAdapter = true)
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
) : Parcelable {

  /**
   * Get current page no.
   */
  fun getCurrentPage(): Int =
    if (self.isBlank()) {
      0
    } else {
      Uri.parse(self)?.getQueryParameter("page[number]")?.toInt() ?: 0
    }

  /**
   * Get next page no.
   */
  fun getNextPage(): Int? = if (next == null || getCurrentPage() == 0) {
    null
  } else {
    getCurrentPage() + 1
  }

}
