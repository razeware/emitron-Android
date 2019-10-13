package com.raywenderlich.emitron.model.entity

import androidx.room.Embedded
import androidx.room.Relation

data class GroupEpisodeJoinWithEpisode(

  @Embedded
  val groupEpisodeJoin: GroupEpisodeJoin? = null,

  @Relation(
    parentColumn = "content_id",
    entityColumn = "content_id",
    entity = Content::class
  )
  val episodes: List<ContentWithDomainAndProgression> = emptyList()
)
