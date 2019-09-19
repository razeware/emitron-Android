package com.raywenderlich.emitron.model

import com.squareup.moshi.Json

/**
 * Object to post playback status periodically
 */
data class PlaybackProgress(
  @Json(name = "video_playback_token")
  val videoPlaybackToken: String,
  val progress: Long,
  val seconds: Long
)
