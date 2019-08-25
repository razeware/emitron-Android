package com.raywenderlich.emitron.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ContentTest {

  @Test
  fun getPercentComplete() {
    val datum = Data(attributes = Attributes(percentComplete = 10.0))
    val content = Content(datum = datum)
    assertThat(content.getPercentComplete()).isEqualTo(10)
  }

  @Test
  fun isFinished() {
    val datum = Data()
    val content = Content(datum = datum)
    assertThat(content.isFinished()).isFalse()

    val datum2 = Data(attributes = Attributes(finished = true))
    val content2 = Content(datum = datum2)
    assertThat(content2.isFinished()).isTrue()
  }

  @Test
  fun getChildId() {
    val datum = Data(id = "1")
    val content = Content(datum = datum)

    assertThat(content.getChildId()).isEqualTo("1")
  }

  @Test
  fun getData() {
    val datum = Data(
      id = "1", relationships = Relationships(
        domains = Contents(datum = listOf(Data("2", type = "domains"))),
        progression = Content(datum = Data("3", type = "progressions"))
      )
    )
    val included = listOf(
      Data("2", type = "domains"),
      Data("3", type = "progressions")
    )
    val content = Content(datum = datum, included = included)

    val expectedData = Data(
      id = "1",
      relationships = Relationships(
        domains = Contents(datum = listOf(Data("2", type = "domains"))),
        progression = Content(datum = Data("3", type = "progressions"))
      )
    )
    assertThat(content.getData()).isEqualTo(expectedData)

    val datum2 = Data(id = "1")
    val content2 = Content(datum = datum2)

    assertThat(content2.getData()).isEqualTo(datum2)
  }

  @Test
  fun getGroups() {
    val datum = Data(id = "1")
    val groups = Data("2", type = "groups")
    val included = listOf(groups, Data(id = "3"))
    val content = Content(datum = datum, included = included)

    assertThat(content.getGroups()).isEqualTo(listOf(groups))
  }

  @Test
  fun isTypeScreencast() {
    val datum = Data(id = "1", attributes = Attributes(contentType = "screencast"))
    val content = Content(datum = datum)

    assertThat(content.isTypeScreencast()).isTrue()

    val datum2 = Data(id = "1")
    val content2 = Content(datum = datum2)

    assertThat(content2.isTypeScreencast()).isFalse()
  }

  @Test
  fun newBookmark() {
    val bookmark = Content.newBookmark("1")
    assertThat(bookmark.datum?.type).isEqualTo("bookmarks")
    assertThat(bookmark.datum?.relationships?.content?.getChildId()).isEqualTo("1")
    assertThat(bookmark.datum?.relationships?.content?.datum?.type).isEqualTo("contents")
  }

  @Test
  fun newProgression() {
    val bookmark = Content.newProgression("1")
    assertThat(bookmark.datum?.type).isEqualTo("progressions")
    assertThat(bookmark.datum?.relationships?.content?.getChildId()).isEqualTo("1")
    assertThat(bookmark.datum?.relationships?.content?.datum?.type).isEqualTo("contents")
  }

}
