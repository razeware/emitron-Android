package com.razeware.emitron.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Object to post playback status periodically
 */
@JsonClass(generateAdapter = true)
data class PlaybackProgress(
  /**
   * Playback token
   */
  @Json(name = "video_playback_token")
  val videoPlaybackToken: String,
  /**
   * Progress
   */
  val progress: Long,
  /**
   * Seconds
   */
  val seconds: Long
)
