package com.razeware.emitron.model.entity

import com.google.common.truth.Truth
import com.razeware.emitron.model.Attributes
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.utils.isEqualTo
import org.junit.Test

class DomainTest {

  @Test
  fun toData() {
    val domain = Domain(domainId = "1", name = "Android & Kotlin")

    Truth.assertThat(domain.toData()).isEqualTo(
      Data(
        id = "1",
        type = "domains",
        attributes = Attributes(
          name = "Android & Kotlin",
          level = null
        )
      )
    )
  }

  @Test
  fun listFrom() {
    val result = Domain.listFrom(
      listOf(
        Data(
          id = "1",
          type = "domains",
          attributes = Attributes(name = "iOS and Swift")
        ),
        Data(
          id = "2",
          type = "domains",
          attributes = Attributes(name = "Android and Kotlin")
        )
      )
    )
    result[0].domainId isEqualTo "1"
    result[0].name isEqualTo "iOS and Swift"
    result[1].domainId isEqualTo "2"
    result[1].name isEqualTo "Android and Kotlin"
  }
}
