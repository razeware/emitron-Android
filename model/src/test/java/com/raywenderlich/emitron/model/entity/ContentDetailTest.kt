package com.raywenderlich.emitron.model.entity

import com.raywenderlich.emitron.data.download.createContentDetail
import com.raywenderlich.emitron.data.download.createExpectedContent
import com.raywenderlich.emitron.model.utils.isEqualTo
import org.junit.Test

class ContentDetailTest {

  @Test
  fun toContent() {
    val expected = createExpectedContent()
    val contentDetail = createContentDetail()

    val result = contentDetail.toContent()

    result isEqualTo expected
  }
}
