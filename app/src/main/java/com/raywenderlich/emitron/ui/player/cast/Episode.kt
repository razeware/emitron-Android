package com.raywenderlich.emitron.ui.player.cast

import com.google.android.exoplayer2.util.MimeTypes
import com.raywenderlich.emitron.model.Data

data class Episode(
  val name: String,
  val uri: String,
  val mimeType: String = MimeTypes.APPLICATION_M3U8
) {
  companion object {

    fun fromData(fromData: Data?): Episode? = fromData?.run {
      Episode(
        name = fromData.getName() ?: "",
        uri = fromData.getStreamUrl()
      )
    }
  }
}
