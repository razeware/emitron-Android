package com.raywenderlich.emitron.model.entity

import androidx.room.Embedded
import androidx.room.Relation


data class ContentDomainJoinWithDomain(

  @Embedded
  val contentDomainJoin: ContentDomainJoin? = null,

  @Relation(
    parentColumn = "domain_id",
    entityColumn = "domain_id",
    entity = Domain::class
  )
  val domains: List<Domain> = emptyList()
)
