package com.raywenderlich.emitron.model.entity

import com.raywenderlich.emitron.model.DownloadState
import com.raywenderlich.emitron.model.utils.isEqualTo
import org.junit.Test

class DownloadTest {

  @Test
  fun toDownloadState() {
    val download = createDownload()

    val downloadState = download.toDownloadState()

    downloadState isEqualTo com.raywenderlich.emitron.model.Download(
      progress = 25,
      state = 3,
      failureReason = 0,
      url = "download/1"
    )
  }

  @Test
  fun isCompleted() {
    val download = createDownload(DownloadState.COMPLETED.ordinal)


    download.isCompleted() isEqualTo true
    download.inProgress() isEqualTo false
  }

  @Test
  fun inProgress() {
    val download = createDownload(DownloadState.IN_PROGRESS.ordinal)

    download.isCompleted() isEqualTo false
    download.inProgress() isEqualTo true
  }

  @Test
  fun isPaused() {
    val download = createDownload(DownloadState.PAUSED.ordinal)

    download.isPaused() isEqualTo true
  }
}

fun createDownload(state: Int = DownloadState.COMPLETED.ordinal): Download = Download(
  "1",
  "download/1",
  25,
  state,
  0,
  "createdAt"
)
