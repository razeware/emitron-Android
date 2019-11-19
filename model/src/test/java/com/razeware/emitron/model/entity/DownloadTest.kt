package com.razeware.emitron.model.entity

import com.razeware.emitron.model.DownloadState
import com.razeware.emitron.model.utils.isEqualTo
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Month

class DownloadTest {

  @Test
  fun toDownloadState() {
    val download = createDownload()

    val downloadState = download.toDownloadState()

    downloadState isEqualTo com.razeware.emitron.model.Download(
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

  @Test
  fun with() {
    // Given day is today
    val today = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)
    Download.with("1", today) isEqualTo Download(
      downloadId = "1",
      state = DownloadState.CREATED.ordinal,
      createdAt = "2019-08-11T02:00:00"
    )
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
