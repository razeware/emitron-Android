package com.razeware.emitron.model

import com.google.common.truth.Truth
import org.junit.Test

class DataTypeTest {

  @Test
  fun toRequestFormat() {
    val dataType = DataType.Bookmarks
    Truth.assertThat(dataType.toRequestFormat()).isEqualTo("bookmarks")
  }
}
