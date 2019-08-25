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

  @Test
  fun setProgression() {
    val progressions = listOf(
      Data(attributes = Attributes(finished = true)),
      Data(attributes = Attributes(finished = false))
    )
    val relationShip = Relationships()
    val result = relationShip.setProgression(emptyList())
    assertThat(result).isEqualTo(relationShip)

    val relationShip2 = Relationships(
      progression = Content(datum = Data(attributes = Attributes(finished = true)))
    )
    val result2 = relationShip2.setProgression(progressions)
    assertThat(result2.hasFinishedContent()).isTrue()
  }

  @Test
  fun getBookmarkId() {
    val relationShip = Relationships(
      bookmark = Content(datum = Data(id = "10"))
    )
    assertThat(relationShip.getBookmarkId()).isEqualTo("10")
  }

  @Test
  fun getProgressionId() {
    val relationShip = Relationships(
      progression = Content(datum = Data(id = "11"))
    )
    assertThat(relationShip.getProgressionId()).isEqualTo("11")
  }

  @Test
  fun getGroupedData() {
    val relationShip = Relationships(
      contents = Contents(datum = listOf(Data(id = "10"), Data(id = "11"), Data(id = "12")))
    )
    assertThat(relationShip.getGroupedData()).isEqualTo(
      listOf(
        Data(id = "10"),
        Data(id = "11"),
        Data(id = "12")
      )
    )

  }

  @Test
  fun getGroupedDataIds() {
    val relationShip = Relationships(
      contents = Contents(datum = listOf(Data(id = "10"), Data(id = "11"), Data(id = "12")))
    )
    assertThat(relationShip.getGroupedDataIds()).isEqualTo(
      listOf("10", "11", "12")
    )
  }

  @Test
  fun setContents() {
    val relationShip = Relationships()
    val result = relationShip.setContents(listOf(Data(id = "10"), Data(id = "11"), Data(id = "12")))
    assertThat(result).isEqualTo(
      Relationships(
        contents = Contents(
          datum = listOf(
            Data(id = "10"),
            Data(id = "11"),
            Data(id = "12")
          )
        )
      )
    )
  }
}
