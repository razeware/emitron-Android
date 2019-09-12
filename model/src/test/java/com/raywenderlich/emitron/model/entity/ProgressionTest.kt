package com.raywenderlich.emitron.model.entity

import com.google.common.truth.Truth.assertThat
import com.raywenderlich.emitron.model.Attributes
import com.raywenderlich.emitron.model.Content
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.model.utils.isEqualTo
import org.junit.Test

class ProgressionTest {

  @Test
  fun init() {
    val progression = Progression(progressionId = "1", percentComplete = 99, finished = true)

    // Assertions
    progression.progressionId isEqualTo "1"
    progression.percentComplete isEqualTo 99
    progression.finished isEqualTo true
  }

  @Test
  fun toData() {
    val progression = Progression(progressionId = "1", percentComplete = 99, finished = true)

    assertThat(progression.toData()).isEqualTo(
      Data(
        id = "1",
        type = "progressions",
        attributes = Attributes(
          percentComplete = 99.0,
          finished = true
        )
      )
    )
  }

  @Test
  fun listFrom() {
    val dataList = listOf(
      Content(
        datum = Data(
          id = "1",
          type = "progressions",
          attributes = Attributes(
            percentComplete = 99.0,
            finished = true
          )
        )
      )
    )

    assertThat(Progression.listFrom(dataList)).isEqualTo(
      listOf(Progression(progressionId = "1", percentComplete = 99, finished = true))
    )
  }
}
