package com.razeware.emitron.ui.player

/**
 * Playback state
 */
enum class MediaPlaybackState {
  /**
   * Buffering
   */
  BUFFERING,
  /**
   * Ready
   */
  READY,
  /**
   * Error
   */
  ERROR,
  /**
   * Unknown media
   */
  UNKNOWN_MEDIA,
  /**
   * Low volume
   */
  LOW_VOLUME,
  /**
   * Completed
   */
  COMPLETED,
  /**
   * Idle
   */
  IDLE
}
