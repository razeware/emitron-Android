package com.razeware.emitron.ui.player.cast

import com.google.android.exoplayer2.util.MimeTypes
import com.razeware.emitron.model.Data

/**
 * Episode VO for playback
 */
data class Episode(
  /**
   * Name
   */
  val name: String,
  /**
   * Playback url
   */
  val uri: String,
  /**
   * MimeType for playback from [MimeTypes]
   */
  val mimeType: String
) {
  companion object {

    fun fromData(data: Data?): Episode? = data?.run {
      Episode(
        name = data.getName() ?: "",
        uri = data.getUrl(),
        mimeType = if (data.isDownloaded()) {
          MimeTypes.APPLICATION_MP4
        } else {
          MimeTypes.APPLICATION_M3U8
        }
      )
    }
  }
}
