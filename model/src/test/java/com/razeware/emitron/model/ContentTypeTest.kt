package com.razeware.emitron.model

import com.google.common.truth.Truth.assertThat
import com.razeware.emitron.model.utils.isEqualTo
import org.junit.Test

class ContentTypeTest {

  @Test
  fun isScreenCast() {
    val contentType = ContentType.Screencast
    assertThat(contentType.isScreencast()).isTrue()
  }

  @Test
  fun toRequestFormat() {
    val contentType = ContentType.Screencast
    assertThat(contentType.toRequestFormat()).isEqualTo("screencast")
  }

  @Test
  fun getAllowedContentTypes() {
    val result = ContentType.getAllowedContentTypes()

    result isEqualTo arrayOf("collection", "screencast")
  }

  @Test
  fun getAllowedDownloadTypes() {
    val result = ContentType.getAllowedDownloadTypes()

    result isEqualTo arrayOf("screencast", "episode")
  }

  @Test
  fun getFilterContentTypes() {
    val result = ContentType.getFilterContentTypes()

    result isEqualTo listOf(
      ContentType.Collection,
      ContentType.Screencast,
      ContentType.Professional
    )
  }
}
