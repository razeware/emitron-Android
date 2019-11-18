package com.razeware.emitron.model.utils

import com.google.common.truth.Truth

infix fun Any?.isEqualTo(expected: Any?) {
  Truth.assertThat(this).isEqualTo(expected)
}

