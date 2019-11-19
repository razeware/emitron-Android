package com.razeware.emitron.ui.player

import android.os.Parcelable
import com.razeware.emitron.model.Data
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

/**
 * Check if playlist is available offline
 *
 * @return false if neither the collection, or any of it's episode are downloaded
 */
fun Playlist?.isNotDownloaded(): Boolean =
  null != this &&
      this.collection?.isDownloaded() != true &&
      this.episodes.any { !it.isDownloaded() }
