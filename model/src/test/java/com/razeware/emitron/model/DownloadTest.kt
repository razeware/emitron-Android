package com.raywenderlich.emitron.model

import com.raywenderlich.emitron.model.utils.isEqualTo
import org.junit.Test

class DownloadTest {

  @Test
  fun forCollection() {

    val episodeDownloads = listOf(
      com.raywenderlich.emitron.model.entity.Download(
        "1",
        "download/1",
        98,
        DownloadState.IN_PROGRESS.ordinal,
        0,
        "createdAt"
      ),
      com.raywenderlich.emitron.model.entity.Download(
        "1",
        "download/1",
        0,
        DownloadState.CREATED.ordinal,
        0,
        "createdAt"
      ),
      com.raywenderlich.emitron.model.entity.Download(
        "1",
        "download/1",
        0,
        DownloadState.CREATED.ordinal,
        0,
        "createdAt"
      )
    )
    val download = Download.fromEpisodeDownloads(episodeDownloads)

    download isEqualTo Download(progress = 32, state = DownloadState.IN_PROGRESS.ordinal)
  }

  @Test
  fun forCollection_allEpisodesDownloaded() {

    val episodeDownloads = listOf(
      com.raywenderlich.emitron.model.entity.Download(
        "1",
        "download/1",
        100,
        DownloadState.COMPLETED.ordinal,
        0,
        "createdAt"
      ),
      com.raywenderlich.emitron.model.entity.Download(
        "1",
        "download/1",
        100,
        DownloadState.COMPLETED.ordinal,
        0,
        "createdAt"
      ),
      com.raywenderlich.emitron.model.entity.Download(
        "1",
        "download/1",
        100,
        DownloadState.COMPLETED.ordinal,
        0,
        "createdAt"
      )
    )
    val download = Download.fromEpisodeDownloads(episodeDownloads)

    download isEqualTo Download(progress = 100, state = DownloadState.COMPLETED.ordinal)
  }
}
