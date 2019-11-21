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

    download isEqualTo Download(progress = 32, state = DownloadState.IN_PROGRESS.ordinal)
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

    download isEqualTo Download(progress = 100, state = DownloadState.COMPLETED.ordinal)
  }
}
