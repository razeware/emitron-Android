package com.raywenderlich.emitron.model.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.raywenderlich.emitron.model.Data


data class ContentWithDomain(

  @Embedded
  val content: Content,

  @Relation(
    parentColumn = "content_id",
    entityColumn = "content_id",
    entity = ContentDomainJoin::class
  )
  val domains: List<ContentDomainJoinWithDomain> = emptyList()
) {

  fun toData(): Data = content.toData().addRelationships(
    domains.flatMap {
      it.domains.map { domain ->
        domain.toData()
      }
    }
  )
}
