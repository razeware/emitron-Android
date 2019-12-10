package com.razeware.emitron.ui.player

import com.razeware.emitron.data.createContentData
import com.razeware.emitron.model.Attributes
import com.razeware.emitron.model.Content
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.Relationships
import com.razeware.emitron.utils.isEqualTo
import org.junit.Test

class PlaylistTest {

  @Test
  fun isNotDownloaded() {
    val playlist = createPlaylist()
    playlist.isNotDownloaded() isEqualTo true
  }

  @Test
  fun size() {
    val playlist = createPlaylist()
    playlist.size() isEqualTo 4
    val playlist2 = createPlaylist(episodes = emptyList())
    playlist2.size() isEqualTo 1
  }

  @Test
  fun hasEpisodes() {
    val playlist = createPlaylist()
    playlist.hasEpisodes() isEqualTo true
    val playlist2 = createPlaylist(episodes = emptyList())
    playlist2.hasEpisodes() isEqualTo false
  }

  @Test
  fun updateCurrentEpisode() {
    val playlist = createPlaylist(episodes = emptyList())
    playlist.updateCurrentEpisode(1) isEqualTo playlist

    val playlist2 = createPlaylist()
    playlist2.updateCurrentEpisode(7) isEqualTo playlist2

    val playlist3 = createPlaylist()
    playlist3.updateCurrentEpisode(2) isEqualTo playlist3.copy(currentEpisode = createPlaylistEpisodes()[2])
  }

  private fun createPlaylist(episodes: List<Data> = createPlaylistEpisodes()): Playlist = Playlist(
    createContentData(),
    episodes = episodes,
    currentEpisode = episodes.getOrNull(0)
  )

  private fun createPlaylistEpisodes(): List<Data> = listOf(
    // Given
    Data(
      id = "5",
      type = "contents",
      attributes = Attributes(name = "five"),
      relationships = Relationships(
        progression = Content(
          datum = Data(
            id = "9",
            type = "progressions",
            attributes = Attributes(percentComplete = 10.0)
          )
        )
      )
    ),
    Data(
      id = "6", type = "contents",
      attributes = Attributes(name = "six"),
      relationships = Relationships()
    ),
    Data(
      id = "7", type = "contents",
      attributes = Attributes(name = "seven"),
      relationships = Relationships()
    )
    ,
    Data(
      id = "8", type = "contents",
      attributes = Attributes(name = "eight"),
      relationships = Relationships()
    )
  )
}
