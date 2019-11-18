package com.razeware.emitron.model.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Relation model for joining group episode with episode
 */
data class GroupEpisodeJoinWithEpisode(

  /**
   * Group episode join
   */
  @Embedded
  val groupEpisodeJoin: GroupEpisodeJoin? = null,

  @Relation(
    parentColumn = "content_id",
    entityColumn = "content_id",
    entity = Content::class
  )
  /**
   * Episodes
   */
  val episodes: List<ContentWithDomainAndProgression> = emptyList()
)
