package com.razeware.emitron.ui.collection

import com.razeware.emitron.data.createContentData
import com.razeware.emitron.data.withProgression
import com.razeware.emitron.model.Content
import com.razeware.emitron.model.Download
import com.razeware.emitron.utils.isEqualTo
import org.junit.Before
import org.junit.Test

class CollectionEpisodeDiffCallbackTest {

  private lateinit var callback: CollectionEpisodeDiffCallback

  @Before
  fun setUp() {
    callback = CollectionEpisodeDiffCallback()
  }

  @Test
  fun areItemsTheSame_header() {
    val collectionEpisode = CollectionEpisode(title = "SwiftUI")
    val newCollectionEpisode = CollectionEpisode(title = "SwiftUI")
    callback.areItemsTheSame(collectionEpisode, newCollectionEpisode) isEqualTo true
  }

  @Test
  fun areItemsTheSame_headers_not_equal() {
    val collectionEpisode = CollectionEpisode(title = "SwiftUI")
    val newCollectionEpisode = CollectionEpisode(title = "JetPack Compose")
    callback.areItemsTheSame(collectionEpisode, newCollectionEpisode) isEqualTo false
  }

  @Test
  fun areItemsTheSame_episode() {
    val collectionEpisode = CollectionEpisode(data = createContentData())
    val newCollectionEpisode = CollectionEpisode(data = createContentData())
    callback.areItemsTheSame(collectionEpisode, newCollectionEpisode) isEqualTo true
  }

  @Test
  fun areItemsTheSame_episodes_not_equal() {
    val collectionEpisode = CollectionEpisode(data = createContentData(id = "10"))
    val newCollectionEpisode = CollectionEpisode(data = createContentData())
    callback.areItemsTheSame(collectionEpisode, newCollectionEpisode) isEqualTo false
  }


  @Test
  fun areContentsTheSame_header() {
    val collectionEpisode = CollectionEpisode(title = "SwiftUI")
    val newCollectionEpisode = CollectionEpisode(title = "SwiftUI")
    callback.areContentsTheSame(collectionEpisode, newCollectionEpisode) isEqualTo true
  }


  @Test
  fun areContentsTheSame_episode_progression() {
    val collectionEpisode = CollectionEpisode(
      data = createContentData(
        progression = Content(withProgression())
      )
    )
    val newCollectionEpisode = CollectionEpisode(
      data = createContentData(
        progression = Content(withProgression())
      )
    )
    callback.areContentsTheSame(collectionEpisode, newCollectionEpisode) isEqualTo true
  }

  @Test
  fun areContentsTheSame_episode_progression_not_equal() {
    val collectionEpisode = CollectionEpisode(
      data = createContentData(
        progression = Content(withProgression())
      )
    )
    val newCollectionEpisode = CollectionEpisode(
      data = createContentData(
        progression = Content(withProgression(finished = true))
      )
    )
    callback.areContentsTheSame(collectionEpisode, newCollectionEpisode) isEqualTo true
  }

  @Test
  fun areContentsTheSame_episode_download_state() {
    val collectionEpisode = CollectionEpisode(
      data = createContentData(
        download = Download(state = 1)
      )
    )
    val newCollectionEpisode = CollectionEpisode(
      data = createContentData(
        download = Download(state = 1)
      )
    )
    callback.areContentsTheSame(collectionEpisode, newCollectionEpisode) isEqualTo true
  }

  @Test
  fun areContentsTheSame_episode_download_state_not_equal() {
    val collectionEpisode = CollectionEpisode(
      data = createContentData(
        download = Download(state = 1)
      )
    )
    val newCollectionEpisode = CollectionEpisode(
      data = createContentData(
        download = Download(state = 2)
      )
    )
    callback.areContentsTheSame(collectionEpisode, newCollectionEpisode) isEqualTo false
  }

  @Test
  fun areContentsTheSame_episode_download_progress() {
    val collectionEpisode = CollectionEpisode(
      data = createContentData(
        download = Download(progress = 10)
      )
    )
    val newCollectionEpisode = CollectionEpisode(
      data = createContentData(
        download = Download(progress = 10)
      )
    )
    callback.areContentsTheSame(collectionEpisode, newCollectionEpisode) isEqualTo true
  }

  @Test
  fun areContentsTheSame_episode_download_progress_not_equal() {
    val collectionEpisode = CollectionEpisode(
      data = createContentData(
        download = Download(progress = 10)
      )
    )
    val newCollectionEpisode = CollectionEpisode(
      data = createContentData(
        download = Download(progress = 20)
      )
    )
    callback.areContentsTheSame(collectionEpisode, newCollectionEpisode) isEqualTo false
  }
}
