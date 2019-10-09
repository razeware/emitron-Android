package com.raywenderlich.emitron.model

import com.google.common.truth.Truth.assertThat
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
}
