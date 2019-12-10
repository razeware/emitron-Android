package com.razeware.emitron.model.entity

import com.razeware.emitron.model.Download
import com.razeware.emitron.model.createContent
import com.razeware.emitron.model.createContentDetail
import com.razeware.emitron.model.utils.isEqualTo
import org.junit.Test

class ContentDetailTest {

  @Test
  fun toContent() {
    val expected = createContent(
      download = Download(
        progress = 100,
        state = 3,
        failureReason = 0,
        url = null,
        cached = true
      )
    )
    val contentDetail = createContentDetail()

    val result = contentDetail.toContent()

    result isEqualTo expected
  }

  @Test
  fun getDownload() {
    val contentDetail = createContentDetail()
    contentDetail.getDownload() isEqualTo Download(
      100,
      3,
      0,
      null,
      cached = true
    )
  }

  @Test
  fun getDownloadIds() {
    val contentDetail = createContentDetail()
    contentDetail.getDownloadIds() isEqualTo listOf("1")
  }
}
