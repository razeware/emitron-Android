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
  val mimeType: String,

  /**
   * Playback progress
   */
  val progress: Long,

  /**
   * Is episode completed
   */
  val isCompleted: Boolean
) {

  /**
   * Get episode progress
   */
  fun getProgressInMillis(): Long = if (isCompleted) 0 else progress * MILLIS_IN_A_SECOND

  companion object {

    /**
     * Millis in a second
     */
    const val MILLIS_IN_A_SECOND: Int = 1000

    /**
     * @return [Episode]
     */
    fun fromData(data: Data): Episode = data.run {
      Episode(
        name = data.getName() ?: "",
        uri = data.getUrl(),
        progress = data.getProgressionProgress(),
        isCompleted = data.isProgressionFinished(),
        mimeType = if (data.isDownloaded()) {
          MimeTypes.APPLICATION_MP4
        } else {
          MimeTypes.APPLICATION_M3U8
        }
      )
    }
  }
}
