package com.raywenderlich.emitron.model.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.raywenderlich.emitron.model.Data


data class ContentWithDomainAndProgression(

  @Embedded
  val content: Content,

  @Relation(
    parentColumn = "content_id",
    entityColumn = "content_id",
    entity = ContentDomainJoin::class
  )
  val domains: List<ContentDomainJoinWithDomain> = emptyList(),

  @Relation(
    parentColumn = "progression_id",
    entityColumn = "progression_id",
    entity = Progression::class
  )
  val progressions: List<Progression> = emptyList()
) {

  fun toData(): Data = content.toData().addRelationships(
    domains.flatMap {
      it.domains.map { domain ->
        domain.toData()
      }
    }.plus(progressions.first().toData())
  )
}
