package com.razeware.emitron.ui.player

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player

/**
 * DTO for [Player] state
 */
data class PlayerState(
  /**
   * Current playback position
   */
  val playbackPositionMs: Long = C.TIME_UNSET,
  /**
   * Current window index
   */
  val windowIndex: Int = C.INDEX_UNSET,
  /**
   * Current auto playback
   */
  val playWhenReady: Boolean = false
) {

  companion object {
    /**
     * Create player state from [Player] and item index
     */
    fun from(player: Player?, itemIndex: Int): PlayerState {
      val playerState = PlayerState(
        playbackPositionMs = player?.currentPosition ?: C.TIME_UNSET,
        windowIndex = player?.currentWindowIndex ?: C.INDEX_UNSET,
        playWhenReady = player?.playWhenReady ?: true
      )
      return if (playerState.windowIndex != itemIndex) {
        playerState.copy(
          playbackPositionMs = C.TIME_UNSET,
          windowIndex = itemIndex
        )
      } else {
        playerState
      }
    }
  }
}

/**
 * DTO for [Player] config
 */
data class PlayerConfig(
  /**
   * Override auto playback
   */
  val overridePlayWhenReady: Boolean = false,
  /**
   * Skip to duration
   */
  val duration: Long = 0,
  /**
   * reset player
   */
  val reset: Boolean = false
)
