package com.razeware.emitron.model.entity

import com.razeware.emitron.model.*
import com.razeware.emitron.model.Download
import com.razeware.emitron.model.utils.isEqualTo
import org.junit.Test

class DownloadWithContentTest {

  @Test
  fun getVideoId() {
    val download = createDownloadWithContent()

    download.getVideoId() isEqualTo "1"
  }

  @Test
  fun getContentId() {
    val download = createDownloadWithContent()

    download.getContentId() isEqualTo "1"
  }

  @Test
  fun getContentName() {
    val download = createDownloadWithContent()

    download.getContentName() isEqualTo "Introduction to Kotlin Lambdas: Getting Started"
  }

  @Test
  fun getDownloadId() {
    val download = createDownloadWithContent()

    download.getDownloadId() isEqualTo "1"
  }

  @Test
  fun isDownloading() {
    val download = createDownloadWithContent(
      createDownload(state = DownloadState.IN_PROGRESS.ordinal)
    )

    download.inProgress() isEqualTo true
  }

  @Test
  fun isPaused() {
    val download = createDownloadWithContent(
      createDownload(state = DownloadState.PAUSED.ordinal)
    )

    download.isPaused() isEqualTo true
  }

  @Test
  fun isCompleted() {
    val download = createDownloadWithContent(
      createDownload(state = DownloadState.COMPLETED.ordinal)
    )

    download.isCompleted() isEqualTo true
  }

  @Test
  fun hasFailed() {
    val download = createDownloadWithContent(
      createDownload(state = DownloadState.FAILED.ordinal)
    )

    download.hasFailed() isEqualTo true
  }

  @Test
  fun toDataScreencast() {
    val download = createDownloadWithContent(
      contentType = "screencast"
    )

    val expectedDownload = Download(
      25,
      3,
      0,
      "download/1"
    )
    val expectedData = buildContentData(
      withRelationship(
        withRelatedBookmark(),
        withRelatedDomains()
      ),
      download = expectedDownload,
      contentType = "screencast"
    )
    download.toData() isEqualTo expectedData
  }

  @Test
  fun toDataCollection() {
    val download = createDownloadWithContent(
      contentType = "collection"
    )

    val expectedDownload = Download(
      100,
      3,
      0,
      null,
      cached = true
    )
    val expectedData = buildContentData(
      withRelationship(
        withRelatedBookmark(),
        withRelatedDomains()
      ),
      download = expectedDownload,
      contentType = "collection"
    )
    download.toData() isEqualTo expectedData
  }

  @Test
  fun getDownloadIds() {
    val download = createDownloadWithContent(
      contentType = "collection"
    )
    download.getDownloadIds() isEqualTo listOf("1", "1")
  }
}
