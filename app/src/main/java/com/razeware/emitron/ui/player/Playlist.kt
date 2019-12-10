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
      this.collection?.isCached() != true &&
      this.episodes.any { !it.isDownloaded() }

/**
 * Playlist items
 */
fun Playlist?.size(): Int {
  this ?: return 0
  return if (this.episodes.isNullOrEmpty()) {
    1
  } else {
    this.episodes.size
  }
}

/**
 * Playlist items
 */
fun Playlist?.hasEpisodes(): Boolean =
  null != this &&
      !this.episodes.isNullOrEmpty()

/**
 * Update current episode by position
 *
 * @param position Int
 */
fun Playlist.updateCurrentEpisode(position: Int): Playlist {
  if (!hasEpisodes() || position > size()) {
    return this
  }
  return copy(currentEpisode = episodes[position])
}


