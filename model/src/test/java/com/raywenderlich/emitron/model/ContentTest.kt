package com.raywenderlich.emitron.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ContentTest {

  @Test
  fun getPercentComplete() {
    val datum = Data(attributes = Attributes(percentComplete = 10.0))
    val content = Content(datum = datum)
    assertThat(content.getPercentComplete()).isEqualTo(10)
  }

  @Test
  fun isFinished() {
    val datum = Data()
    val content = Content(datum = datum)
    assertThat(content.isFinished()).isFalse()

    val datum2 = Data(attributes = Attributes(finished = true))
    val content2 = Content(datum = datum2)
    assertThat(content2.isFinished()).isTrue()
  }
}
