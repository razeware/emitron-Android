package com.razeware.emitron.model.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.razeware.emitron.model.Data

/**
 * Relation to represent [Content] with [Domain]
 */
data class ContentWithDomain(

  /**
   * Content
   */
  @Embedded
  val content: Content,

  /**
   * List of domains
   */
  @Relation(
    parentColumn = "content_id",
    entityColumn = "content_id",
    entity = ContentDomainJoin::class
  )
  val domains: List<ContentDomainJoinWithDomain> = emptyList()
) {

  /**
   * Create [Data] from [ContentWithDomain]
   */
  fun toData(): Data = content.toData().addRelationships(
    domains.flatMap {
      it.domains.map { domain ->
        domain.toData()
      }
    }
  )
}
