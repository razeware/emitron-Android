package com.raywenderlich.emitron.ui.player

import android.os.Parcelable
import com.raywenderlich.emitron.model.Data
import kotlinx.android.parcel.Parcelize

/**
 * DTO for playback items from collection detail view to player view
 */
@Parcelize
data class Playlist(
  /**
   * Collection (Screen cast or Video course)
   */
  val collection: Data?,
  /**
   * Episodes
   */
  val episodes: List<Data>,
  /**
   * Current episode (Starting position)
   */
  val currentEpisode: Data? = null
) : Parcelable
