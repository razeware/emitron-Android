package com.razeware.emitron.model

import com.google.common.truth.Truth.assertThat
import com.razeware.emitron.model.utils.isEqualTo
import org.junit.Test

class RelationshipsTest {

  @Test
  fun getDomainName() {
    val domains = listOf(
      Data(attributes = Attributes(name = "iOS & Swift")),
      Data(attributes = Attributes(name = "Android & Kotlin"))
    )
    val domainNames = "iOS & Swift, Android & Kotlin"
    val relationship = Relationships(domains = Contents(datum = domains))
    assertThat(relationship.getDomainName()).isEqualTo(domainNames)
  }

  @Test
  fun hasFinishedContent() {
    val progression = Content(datum = Data(attributes = Attributes(finished = true)))
    val relationship = Relationships(progression = progression)
    assertThat(relationship.hasFinishedContent()).isTrue()

    val relationship2 = Relationships(Content())
    assertThat(relationship2.hasFinishedContent()).isFalse()
  }

  @Test
  fun updateProgressionFinished() {
    val progression = Content(datum = Data(attributes = Attributes(finished = true)))
    val relationship = Relationships(progression = progression)
    relationship.hasFinishedContent() isEqualTo true
  }

  @Test
  fun getPercentComplete() {
    val progression = Content(datum = Data(attributes = Attributes(percentComplete = 10.0)))
    val relationship = Relationships(progression = progression)
    assertThat(relationship.getPercentComplete()).isEqualTo(10)

    val relationship2 = Relationships(Content())
    assertThat(relationship2.getPercentComplete()).isEqualTo(0)
  }

  @Test
  fun getProgressionProgress() {
    val progression = Content(datum = Data(attributes = Attributes(progress = 150)))
    val relationship = Relationships(progression = progression)
    assertThat(relationship.getProgressionProgress()).isEqualTo(150)

    val relationship2 = Relationships(Content())
    assertThat(relationship2.getProgressionProgress()).isEqualTo(0)
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
    val relationship = Relationships()
    val result = relationship.updateDomains(emptyList())
    assertThat(result).isEqualTo(relationship)

    val relationship2 = Relationships(
      domains = Contents(
        listOf(
          Data(
            id = "1",
            type = "domains"
          )
        )
      )
    )
    val result2 = relationship2.updateDomains(domains)
    assertThat(result2.getDomainName()).isEqualTo("iOS & Swift")
  }

  @Test
  fun updateProgression() {
    val progressions = listOf(
      Data(attributes = Attributes(finished = true)),
      Data(attributes = Attributes(finished = false))
    )
    val relationship = Relationships()
    val result = relationship.updateProgression("1", emptyList())
    assertThat(result).isEqualTo(relationship)

    val relationship2 = Relationships(
      progression = Content(datum = Data(attributes = Attributes(finished = true)))
    )
    val result2 = relationship2.updateProgression("1", progressions)
    assertThat(result2.hasFinishedContent()).isTrue()
  }

  @Test
  fun updateProgression_forLocalProgress() {
    val progressions = listOf(
      Data(
        id = "1",
        type = "progressions",
        attributes = Attributes(finished = true),
        relationships = Relationships(content = Content(datum = Data(id = "1")))
      ),
      Data(
        id = "1",
        type = "progressions",
        attributes = Attributes(finished = false, contentId = "2"),
        relationships = Relationships(content = Content(datum = Data(id = "2")))
      )
    )
    val relationship = Relationships()
    val result = relationship.updateProgression("1", progressions)
    result.hasFinishedContent() isEqualTo true
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
    val relationship = Relationships()
    val result = relationship.updateBookmark(emptyList())
    assertThat(result).isEqualTo(relationship)

    val relationship2 = Relationships(
      bookmark = Content(datum = Data(id = "1", type = "bookmarks"))
    )
    val result2 = relationship2.updateBookmark(bookmarks)
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
    val relationship = Relationships()
    val result = relationship.addDomains(emptyList())
    assertThat(result).isEqualTo(relationship)

    val relationship2 = Relationships()
    val result2 = relationship2.addDomains(domains)
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
    val relationship = Relationships()
    val result = relationship.addContents(emptyList())
    assertThat(result).isEqualTo(relationship)

    val relationship2 = Relationships()
    val result2 = relationship2.addContents(contents)
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
    val relationship = Relationships()
    val result = relationship.addContents(emptyList())
    assertThat(result).isEqualTo(relationship)

    val relationship2 = Relationships()
    val result2 = relationship2.addContentGroups(contents)
    assertThat(result2.getContentGroupIds()).isEqualTo(listOf("1"))
  }

  @Test
  fun addProgression() {
    val progressions = listOf(
      Data(id = "1", type = "progressions", attributes = Attributes(finished = true)),
      Data(type = "progressions", attributes = Attributes(finished = false))
    )
    val relationship = Relationships()
    val result = relationship.addProgression(emptyList())
    assertThat(result).isEqualTo(relationship)

    val relationship2 = Relationships()
    val result2 = relationship2.addProgression(progressions)
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
    val relationship = Relationships()
    val result = relationship.addBookmark(emptyList())
    assertThat(result).isEqualTo(relationship)

    val relationship2 = Relationships()
    val result2 = relationship2.addBookmark(bookmarks)
    assertThat(result2.getBookmarkId()).isEqualTo("1")

    val relationship3 = Relationships()
    val result3 = relationship3.addBookmark("1")
    assertThat(result3.getBookmarkId()).isEqualTo("1")
  }

  @Test
  fun getBookmarkId() {
    val relationship = Relationships(
      bookmark = Content(datum = Data(id = "10"))
    )
    assertThat(relationship.getBookmarkId()).isEqualTo("10")
  }

  @Test
  fun getProgressionId() {
    val relationship = Relationships(
      progression = Content(datum = Data(id = "11"))
    )
    assertThat(relationship.getProgressionId()).isEqualTo("11")
  }

  @Test
  fun getContentId() {
    val relationship = Relationships(
      content = Content(datum = Data(id = "12"))
    )
    assertThat(relationship.getContentId()).isEqualTo("12")
  }


  @Test
  fun getDomainIds() {
    val relationship = Relationships(
      domains = Contents(
        datum = listOf(
          Data(id = "10"),
          Data(id = "11"),
          Data(id = "12")
        )
      )
    )
    assertThat(relationship.getDomainIds()).isEqualTo(listOf("10", "11", "12"))
  }

  @Test
  fun getGroupedData() {
    val relationship = Relationships(
      contents = Contents(datum = listOf(Data(id = "10"), Data(id = "11"), Data(id = "12")))
    )
    assertThat(relationship.getChildContents()).isEqualTo(
      listOf(
        Data(id = "10"),
        Data(id = "11"),
        Data(id = "12")
      )
    )

  }

  @Test
  fun getGroupedDataIds() {
    val relationship = Relationships(
      contents = Contents(datum = listOf(Data(id = "10"), Data(id = "11"), Data(id = "12")))
    )
    assertThat(relationship.getChildContentIds()).isEqualTo(
      listOf("10", "11", "12")
    )
  }

  @Test
  fun setContents() {
    val relationship = Relationships()
    val result = relationship.setContents(listOf(Data(id = "10"), Data(id = "11"), Data(id = "12")))
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
