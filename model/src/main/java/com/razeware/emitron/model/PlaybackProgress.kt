package com.razeware.emitron.model

import com.squareup.moshi.Json

/**
 * Object to post playback status periodically
 */
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
