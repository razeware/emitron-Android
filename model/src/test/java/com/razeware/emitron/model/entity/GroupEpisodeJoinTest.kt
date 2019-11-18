package com.razeware.emitron.model.entity

import com.razeware.emitron.model.*
import com.razeware.emitron.model.Content
import com.razeware.emitron.model.utils.isEqualTo
import org.junit.Test

class GroupEpisodeJoinTest {

  @Test
  fun init() {
    val groupEpisodeJoin = GroupEpisodeJoin("1", "2")

    groupEpisodeJoin.episodeId isEqualTo "2"
    groupEpisodeJoin.groupId isEqualTo "1"
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
          id = "1", type = "groups", relationships = Relationships(
            contents = Contents(
              datum = listOf(
                Data(id = "7", type = "contents"),
                Data(id = "9", type = "contents")
              )
            )
          )
        ),
        Data(
          id = "2", type = "groups", relationships = Relationships(
            contents = Contents(
              datum = listOf(
                Data(id = "8", type = "contents"),
                Data(id = "10", type = "contents")
              )
            )
          )
        ),
        Data(id = "7", type = "contents"),
        Data(id = "8", type = "contents")
      )
    )

    val result = GroupEpisodeJoin.listFrom(content)

    result isEqualTo listOf(
      GroupEpisodeJoin("1", "7"),
      GroupEpisodeJoin("1", "9"),
      GroupEpisodeJoin("2", "8"),
      GroupEpisodeJoin("2", "10")
    )
  }
}
