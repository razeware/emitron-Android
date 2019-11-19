package com.razeware.emitron.model.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Relation object for [ContentGroupJoin], and groups [Group], and episodes [Content]
 */
data class ContentGroupJoinWithGroup(

  /**
   * Content group join
   */
  @Embedded
  val contentGroupJoin: ContentGroupJoin? = null,

  /**
   * Groups
   */
  @Relation(
    parentColumn = "group_id",
    entityColumn = "group_id",
    entity = Group::class
  )
  val groups: List<Group> = emptyList(),

  /**
   * Episodes
   */
  @Relation(
    parentColumn = "group_id",
    entityColumn = "group_id",
    entity = GroupEpisodeJoin::class
  )
  val episodes: List<GroupEpisodeJoinWithEpisode> = emptyList()
)

