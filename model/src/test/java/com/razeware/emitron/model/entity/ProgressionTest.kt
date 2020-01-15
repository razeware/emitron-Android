package com.razeware.emitron.model.entity

import com.google.common.truth.Truth.assertThat
import com.razeware.emitron.model.Attributes
import com.razeware.emitron.model.Content
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.Relationships
import com.razeware.emitron.model.utils.isEqualTo
import org.junit.Test

class ProgressionTest {

  @Test
  fun init() {
    val progression =
      Progression(contentId = "1", progressionId = "1", percentComplete = 99, finished = true)

    // Assertions
    progression.progressionId isEqualTo "1"
    progression.percentComplete isEqualTo 99
    progression.finished isEqualTo true
  }

  @Test
  fun toData() {
    val progression =
      Progression(contentId = "1", progressionId = "1", percentComplete = 99, finished = true)

    assertThat(progression.toData()).isEqualTo(
      Data(
        id = "1",
        type = "progressions",
        attributes = Attributes(
          percentComplete = 99.0,
          finished = true,
          progress = 0,
          contentId = "1"
        ),
        relationships = Relationships(
          content = Content(
            datum = Data(id = "1")
          )
        )
      )
    )
  }

  @Test
  fun listFrom() {
    val dataList = listOf(
      Data(
        id = "1",
        relationships = Relationships(
          progression = Content(
            datum = Data(
              id = "1",
              type = "progressions",
              attributes = Attributes(
                percentComplete = 99.0,
                finished = true,
                contentId = "1"
              ),
              relationships = Relationships(
                content = Content(
                  datum = Data(id = "1")
                )
              )
            )
          )
        )
      )
    )

    assertThat(Progression.listFrom(dataList)).isEqualTo(
      listOf(
        Progression(
          progressionId = "1",
          contentId = "1",
          percentComplete = 99,
          finished = true,
          synced = true,
          updatedAt = ""
        )
      )
    )
  }

  @Test
  fun listFromIncluded() {
    val dataList = listOf(
      Data(
        id = "1",
        type = "progressions",
        attributes = Attributes(
          percentComplete = 99.0,
          finished = true
        ),
        relationships = Relationships(
          content = Content(
            datum = Data(id = "1")
          )
        )
      ),
      Data(
        id = "1",
        type = "groups"
      ),
      Data(
        id = "3",
        type = "progressions",
        attributes = Attributes(
          percentComplete = 11.0,
          finished = false
        ),
        relationships = Relationships(
          content = Content(
            datum = Data(id = "2")
          )
        )
      )
    )

    val result = Progression.listFromIncluded(dataList)

    result isEqualTo
        listOf(
          Progression(
            contentId = "1",
            progressionId = "1",
            percentComplete = 99,
            finished = true,
            synced = true,
            updatedAt = ""
          ),
          Progression(
            contentId = "2",
            progressionId = "3",
            percentComplete = 11,
            finished = false,
            synced = true,
            updatedAt = ""
          )
        )
  }
}
