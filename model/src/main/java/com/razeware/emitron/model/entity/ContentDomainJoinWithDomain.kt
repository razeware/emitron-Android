package com.razeware.emitron.model.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Relation model for joining content domain join with domain
 */
data class ContentDomainJoinWithDomain(

  /**
   * Content domain join
   */
  @Embedded
  val contentDomainJoin: ContentDomainJoin? = null,

  @Relation(
    parentColumn = "domain_id",
    entityColumn = "domain_id",
    entity = Domain::class
  )
  /**
   * Domains
   */
  val domains: List<Domain> = emptyList()
)
