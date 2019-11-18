package com.razeware.emitron.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.razeware.emitron.model.Data

@Entity(
  tableName = ContentDomainJoin.TABLE_NAME,
  indices = [Index("content_id"), Index("domain_id")],
  primaryKeys = ["content_id", "domain_id"],
  foreignKeys = [ForeignKey(
    entity = Content::class,
    parentColumns = arrayOf("content_id"),
    childColumns = arrayOf("content_id")
  ), ForeignKey(
    entity = Domain::class,
    parentColumns = arrayOf("domain_id"),
    childColumns = arrayOf("domain_id")
  )]
)
/**
 * Entity for [Content] [Domain] relations
 */
data class ContentDomainJoin(
  /**
   * Content Id
   */
  @ColumnInfo(name = "content_id")
  val contentId: String,
  /**
   * Domain Id
   */
  @ColumnInfo(name = "domain_id")
  val domainId: String
) {
  companion object {

    /**
     * Table name for content domain relations
     */
    const val TABLE_NAME: String = "content_domain_join"

    /**
     * @return list of [ContentDomainJoin] from list of [Data]
     */
    fun listFrom(contents: List<Data>): List<ContentDomainJoin> =
      contents.flatMap { content ->
        content.getDomainIds().map { domainId ->
          ContentDomainJoin(
            contentId = content.id!!,
            domainId = domainId
          )
        }
      }

    /**
     * @return list of [ContentDomainJoin] from [Data]
     */
    fun from(data: Data): List<ContentDomainJoin> =
      data.getDomainIds().map { domainId ->
        ContentDomainJoin(
          contentId = data.id!!,
          domainId = domainId
        )
      }
  }
}
