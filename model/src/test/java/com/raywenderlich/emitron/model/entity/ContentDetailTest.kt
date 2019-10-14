package com.raywenderlich.emitron.model.entity

import com.raywenderlich.emitron.model.Download
import com.raywenderlich.emitron.model.createContent
import com.raywenderlich.emitron.model.createContentDetail
import com.raywenderlich.emitron.model.utils.isEqualTo
import org.junit.Test

class ContentDetailTest {

  @Test
  fun toContent() {
    val expected = createContent()
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
      null
    )
  }

  @Test
  fun getDownloadIds() {
    val contentDetail = createContentDetail()
    contentDetail.getDownloadIds() isEqualTo listOf("1")
  }
}
