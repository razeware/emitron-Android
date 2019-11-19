package com.razeware.emitron.model.entity

import com.razeware.emitron.model.utils.isEqualTo
import org.junit.Test

class ContentGroupJoinTest {

  @Test
  fun listFrom() {
    val result =
      ContentGroupJoin.listFrom(
        "1", listOf(
          Group("1", "The basics", 1),
          Group("2", "The advanced", 2)
        )
      )

    result isEqualTo listOf(
      ContentGroupJoin("1", "1"),
      ContentGroupJoin("1", "2")
    )
  }
}
