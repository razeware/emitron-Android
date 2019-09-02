package com.raywenderlich.emitron.model

import org.junit.Test

class DomainTest {

  @Test
  fun listFrom() {
    val result = Domain.listFrom(listOf(
        Data(id = "1",
            attributes = Attributes(name = "iOS and Swift")),
        Data(id = "2",
            attributes = Attributes(name = "Android and Kotlin")))
    )
    result[0].domainId = "1"
    result[0].name = "iOS and Swift"
    result[0].domainId = "2"
    result[0].name = "Android and Kotlin"
  }
}
