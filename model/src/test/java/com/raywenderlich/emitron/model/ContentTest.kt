package com.raywenderlich.emitron.model

import com.google.common.truth.Truth.assertThat
import com.raywenderlich.emitron.model.utils.isEqualTo
import org.junit.Test

class ContentTest {

  @Test
  fun getPercentComplete() {
    val datum = Data(attributes = Attributes(percentComplete = 10.0))
    val content = Content(datum = datum)
    assertThat(content.getPercentComplete()).isEqualTo(10)
  }

  @Test
  fun getProgress() {
    val datum = Data(attributes = Attributes(progress = 10L))
    val content = Content(datum = datum)
    assertThat(content.getProgress()).isEqualTo(10L)
  }

  @Test
  fun getPlayerToken() {
    val datum = Data(attributes = Attributes(videoPlaybackToken = "Sam D"))
    val content = Content(datum = datum)
    assertThat(content.getPlayerToken()).isEqualTo("Sam D")
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

    val content2 = Content()
    assertThat(content2.getChildId()).isNull()
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
  fun getContentGroupIds() {
    val datum = Data(
      id = "1", relationships = Relationships(
        groups = Contents(
          datum = listOf(Data(id = "1"), Data("2"))
        )
      )
    )
    val groups = Data("2", type = "groups")
    val included = listOf(groups, Data(id = "3"))
    val content = Content(datum = datum, included = included)

    assertThat(content.getContentGroupIds()).isEqualTo(listOf("1", "2"))
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
  fun isTypeCollection() {
    val datum = Data(id = "1", attributes = Attributes(contentType = "collection"))
    val content = Content(datum = datum)

    assertThat(content.isTypeCollection()).isTrue()

    val datum2 = Data(id = "1")
    val content2 = Content(datum = datum2)

    assertThat(content2.isTypeCollection()).isFalse()
  }

  @Test
  fun getEpisodeIds() {
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
          id = "1", type = "groups", relationships = Relationships(
            contents = Contents(
              datum = listOf(
                Data(id = "7", type = "contents")
              )
            )
          )
        ),
        Data(
          id = "2", type = "groups", relationships = Relationships(
            contents = Contents(
              datum = listOf(
                Data(id = "8", type = "contents")
              )
            )
          )
        ),
        Data(id = "7", type = "contents"),
        Data(id = "8", type = "contents")
      )
    )

    content.getEpisodeIds() isEqualTo listOf("7", "8")
  }

  @Test
  fun getIncludedProgressions() {
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
        Data(id = "7", type = "progressions"),
        Data(id = "8", type = "progressions")
      )
    )

    content.getIncludedProgressions() isEqualTo listOf(
      Data(id = "7", type = "progressions"),
      Data(id = "8", type = "progressions")
    )
  }

  @Test
  fun getVideoId() {
    val datum = Data(id = "1", attributes = Attributes(videoId = "1"))
    val content = Content(datum = datum)

    content.getVideoId() isEqualTo "1"
  }

  @Test
  fun newBookmark() {
    val bookmark = Content.newBookmark("1")
    assertThat(bookmark.datum?.type).isEqualTo("bookmarks")
    assertThat(bookmark.datum?.relationships?.content?.getChildId()).isEqualTo("1")
    assertThat(bookmark.datum?.relationships?.content?.datum?.type).isEqualTo("contents")
  }
}
