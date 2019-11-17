package com.razeware.emitron.model.entity

import com.razeware.emitron.model.*
import com.razeware.emitron.model.Content
import com.razeware.emitron.model.utils.isEqualTo
import org.junit.Test

class GroupTest {

  @Test
  fun toData() {
    val group = Group(
      "1",
      "The framework",
      2
    )

    val result = group.toData()

    result isEqualTo Data(
      id = "1",
      type = "groups",
      attributes = Attributes(
        name = "The framework",
        ordinal = 2
      )
    )
  }

  @Test
  fun listFrom() {
    val datum = Data(
      id = "1",
      attributes = Attributes(contentType = "screencast"),
      relationships = Relationships(
        groups = Contents(
          datum = listOf(Data(id = "1"), Data("2"))
        )
      )
    )
    val content = Content(
      datum = datum,
      included = listOf(
        Data(
          id = "1",
          type = "groups",
          attributes = Attributes(
            name = "The basics",
            ordinal = 1
          )
        ),
        Data(
          id = "2",
          type = "groups",
          attributes = Attributes(
            name = "The advanced",
            ordinal = 2
          )
        )
      )
    )

    val result = Group.listFrom(content)

    result isEqualTo listOf(
      Group("1", "The basics", 1),
      Group("2", "The advanced", 2)
    )
  }
}
