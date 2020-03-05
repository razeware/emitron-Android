package com.razeware.emitron.model

import com.razeware.emitron.model.utils.isEqualTo
import org.junit.Test

class DownloadTest {

  @Test
  fun fromEpisodeDownloads() {

    val episodeDownloads = listOf(
      com.razeware.emitron.model.entity.Download(
        "1",
        "download/1",
        98,
        DownloadState.IN_PROGRESS.ordinal,
        0,
        "createdAt"
      ),
      com.razeware.emitron.model.entity.Download(
        "2",
        "download/1",
        0,
        DownloadState.CREATED.ordinal,
        0,
        "createdAt"
      ),
      com.razeware.emitron.model.entity.Download(
        "3",
        "download/1",
        0,
        DownloadState.CREATED.ordinal,
        0,
        "createdAt"
      )
    )
    val download = Download.fromEpisodeDownloads(episodeDownloads, listOf("1", "2", "3"))

    download isEqualTo Download(
      progress = 32,
      state = DownloadState.PAUSED.ordinal,
      cached = true
    )
  }

  @Test
  fun fromEpisodeDownloads_allEpisodesDownloaded() {

    val episodeDownloads = listOf(
      com.razeware.emitron.model.entity.Download(
        "1",
        "download/1",
        100,
        DownloadState.COMPLETED.ordinal,
        0,
        "createdAt"
      ),
      com.razeware.emitron.model.entity.Download(
        "2",
        "download/1",
        100,
        DownloadState.COMPLETED.ordinal,
        0,
        "createdAt"
      ),
      com.razeware.emitron.model.entity.Download(
        "3",
        "download/1",
        100,
        DownloadState.COMPLETED.ordinal,
        0,
        "createdAt"
      )
    )
    val download = Download.fromEpisodeDownloads(episodeDownloads, listOf("1", "2", "3"))

    download isEqualTo Download(
      progress = 100,
      state = DownloadState.COMPLETED.ordinal,
      cached = true
    )
  }

  @Test
  fun isDownloaded() {
    val download = Download(state = 2)
    download.isDownloaded() isEqualTo false

    val download2 = Download(progress = 100, state = 3)
    download2.isDownloaded() isEqualTo true
  }

  @Test
  fun isCached() {
    val download = Download()
    download.isCached() isEqualTo false

    val download2 = Download(cached = true)
    download2.isCached() isEqualTo true
  }

  @Test
  fun isDownloading() {
    val download = Download()
    download.isDownloading() isEqualTo false

    val download2 = Download(state = 2)
    download2.isDownloading() isEqualTo true
  }

  @Test
  fun isPending() {
    val download = Download()
    download.isPending() isEqualTo false

    val download2 = Download(state = 1)
    download2.isPending() isEqualTo true
  }

  @Test
  fun isFailed() {
    val download = Download()
    download.isFailed() isEqualTo false

    val download2 = Download(state = 4)
    download2.isFailed() isEqualTo true
  }

  @Test
  fun isPaused() {
    val download = Download()
    download.isPaused() isEqualTo false

    val download2 = Download(state = 5)
    download2.isPaused() isEqualTo true
  }

  @Test
  fun getProgress() {
    val download = Download(progress = 70)
    download.getProgress() isEqualTo 70

    val download2: Download? = null
    download2.getProgress() isEqualTo 0
  }

  @Test
  fun getState() {
    val download = Download(progress = 100)

    download.getState() isEqualTo 3
  }
}
