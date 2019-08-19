package com.raywenderlich.emitron.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RelationshipsTest {

  @Test
  fun getDomainName() {
    val domains = listOf(
      Data(attributes = Attributes(name = "iOS & Swift")),
      Data(attributes = Attributes(name = "Android & Kotlin"))
    )
    val domainNames = "iOS & Swift, Android & Kotlin"
    val relationShip = Relationships(domains = Contents(datum = domains))
    assertThat(relationShip.getDomainName()).isEqualTo(domainNames)
  }

  @Test
  fun hasFinishedContent() {
    val progression = Content(datum = Data(attributes = Attributes(finished = true)))
    val relationShip = Relationships(progression = progression)
    assertThat(relationShip.hasFinishedContent()).isTrue()

    val relationShip2 = Relationships(Content())
    assertThat(relationShip2.hasFinishedContent()).isFalse()
  }

  @Test
  fun getPercentComplete() {
    val progression = Content(datum = Data(attributes = Attributes(percentComplete = 10.0)))
    val relationShip = Relationships(progression = progression)
    assertThat(relationShip.getPercentComplete()).isEqualTo(10)

    val relationShip2 = Relationships(Content())
    assertThat(relationShip2.getPercentComplete()).isEqualTo(0)
  }

  @Test
  fun setDomains() {
    val domains = listOf(
      Data(id = "1", attributes = Attributes(name = "iOS & Swift")),
      Data(attributes = Attributes(name = "Android & Kotlin"))
    )
    val relationShip = Relationships()
    val result = relationShip.setDomains(emptyList())
    assertThat(result).isEqualTo(relationShip)

    val relationShip2 = Relationships(
      domains = Contents(
        listOf(Data(id = "1"))
      )
    )
    val result2 = relationShip2.setDomains(domains)
    assertThat(result2.getDomainName()).isEqualTo("iOS & Swift")
  }
}
