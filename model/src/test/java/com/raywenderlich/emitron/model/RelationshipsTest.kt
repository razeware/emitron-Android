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
  fun updateDomains() {
    val domains = listOf(
      Data(
        id = "1",
        type = "domains",
        attributes = Attributes(name = "iOS & Swift")
      ),
      Data(attributes = Attributes(name = "Android & Kotlin"))
    )
    val relationShip = Relationships()
    val result = relationShip.updateDomains(emptyList())
    assertThat(result).isEqualTo(relationShip)

    val relationShip2 = Relationships(
      domains = Contents(
        listOf(
          Data(
            id = "1",
            type = "domains"
          )
        )
      )
    )
    val result2 = relationShip2.updateDomains(domains)
    assertThat(result2.getDomainName()).isEqualTo("iOS & Swift")
  }

  @Test
  fun updateProgression() {
    val progressions = listOf(
      Data(attributes = Attributes(finished = true)),
      Data(attributes = Attributes(finished = false))
    )
    val relationShip = Relationships()
    val result = relationShip.updateProgression(emptyList())
    assertThat(result).isEqualTo(relationShip)

    val relationShip2 = Relationships(
      progression = Content(datum = Data(attributes = Attributes(finished = true)))
    )
    val result2 = relationShip2.updateProgression(progressions)
    assertThat(result2.hasFinishedContent()).isTrue()
  }

  @Test
  fun updateBookmark() {
    val bookmarks = listOf(
      Data(
        id = "1",
        type = "bookmarks",
        attributes = Attributes()
      )
    )
    val relationShip = Relationships()
    val result = relationShip.updateBookmark(emptyList())
    assertThat(result).isEqualTo(relationShip)

    val relationShip2 = Relationships(
      bookmark = Content(datum = Data(id = "1", type = "bookmarks"))
    )
    val result2 = relationShip2.updateBookmark(bookmarks)
    assertThat(result2.getBookmarkId()).isEqualTo("1")
  }

  @Test
  fun addDomains() {
    val domains = listOf(
      Data(
        id = "1",
        type = "domains",
        attributes = Attributes(name = "iOS & Swift")
      ),
      Data(attributes = Attributes(name = "Android & Kotlin"))
    )
    val relationShip = Relationships()
    val result = relationShip.addDomains(emptyList())
    assertThat(result).isEqualTo(relationShip)

    val relationShip2 = Relationships()
    val result2 = relationShip2.addDomains(domains)
    assertThat(result2.getDomainName()).isEqualTo("iOS & Swift")
  }

  @Test
  fun addContents() {
    val contents = listOf(
      Data(
        id = "1",
        type = "contents",
        attributes = Attributes(name = "Content1")
      ),
      Data(attributes = Attributes(name = "Content2"))
    )
    val relationShip = Relationships()
    val result = relationShip.addContents(emptyList())
    assertThat(result).isEqualTo(relationShip)

    val relationShip2 = Relationships()
    val result2 = relationShip2.addContents(contents)
    assertThat(result2.getChildContentIds()).isEqualTo(listOf("1"))
  }

  @Test
  fun addContentGroups() {
    val contents = listOf(
      Data(
        id = "1",
        type = "groups",
        attributes = Attributes(name = "Content1")
      ),
      Data(attributes = Attributes(name = "Content2"))
    )
    val relationShip = Relationships()
    val result = relationShip.addContents(emptyList())
    assertThat(result).isEqualTo(relationShip)

    val relationShip2 = Relationships()
    val result2 = relationShip2.addContentGroups(contents)
    assertThat(result2.getContentGroupIds()).isEqualTo(listOf("1"))
  }

  @Test
  fun addProgression() {
    val progressions = listOf(
      Data(id = "1", type = "progressions", attributes = Attributes(finished = true)),
      Data(type = "progressions", attributes = Attributes(finished = false))
    )
    val relationShip = Relationships()
    val result = relationShip.addProgression(emptyList())
    assertThat(result).isEqualTo(relationShip)

    val relationShip2 = Relationships()
    val result2 = relationShip2.addProgression(progressions)
    assertThat(result2.hasFinishedContent()).isTrue()
  }

  @Test
  fun addBookmark() {
    val bookmarks = listOf(
      Data(
        id = "1",
        type = "bookmarks",
        attributes = Attributes()
      )
    )
    val relationShip = Relationships()
    val result = relationShip.addBookmark(emptyList())
    assertThat(result).isEqualTo(relationShip)

    val relationShip2 = Relationships()
    val result2 = relationShip2.addBookmark(bookmarks)
    assertThat(result2.getBookmarkId()).isEqualTo("1")

    val relationShip3 = Relationships()
    val result3 = relationShip3.addBookmark("1")
    assertThat(result3.getBookmarkId()).isEqualTo("1")
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
  fun getContentId() {
    val relationShip = Relationships(
      content = Content(datum = Data(id = "12"))
    )
    assertThat(relationShip.getContentId()).isEqualTo("12")
  }


  @Test
  fun getDomainIds() {
    val relationShip = Relationships(
      domains = Contents(
        datum = listOf(
          Data(id = "10"),
          Data(id = "11"),
          Data(id = "12")
        )
      )
    )
    assertThat(relationShip.getDomainIds()).isEqualTo(listOf("10", "11", "12"))
  }

  @Test
  fun getGroupedData() {
    val relationShip = Relationships(
      contents = Contents(datum = listOf(Data(id = "10"), Data(id = "11"), Data(id = "12")))
    )
    assertThat(relationShip.getChildContents()).isEqualTo(
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
    assertThat(relationShip.getChildContentIds()).isEqualTo(
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

    val relationships2 = Relationships()
    assertThat(relationships2.setContents(emptyList())).isEqualTo(relationships2)
  }
}
