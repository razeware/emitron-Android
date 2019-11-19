package com.razeware.emitron.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
  tableName = GroupEpisodeJoin.TABLE_NAME,
  indices = [Index("group_id"), Index("content_id")],
  primaryKeys = ["group_id", "content_id"],
  foreignKeys = [ForeignKey(
    entity = Group::class,
    parentColumns = arrayOf("group_id"),
    childColumns = arrayOf("group_id")
  ), ForeignKey(
    entity = Content::class,
    parentColumns = arrayOf("content_id"),
    childColumns = arrayOf("content_id")
  )]
)
/**
 * Table for group-episode relation
 */
data class GroupEpisodeJoin(
  /**
   * Group Id
   */
  @ColumnInfo(name = "group_id")
  val groupId: String,
  /**
   * Episode Id
   */
  @ColumnInfo(name = "content_id")
  val episodeId: String
) {
  companion object {

    /**
     * Table name
     */
    const val TABLE_NAME: String = "group_content_join"

    /**
     * List of [GroupEpisodeJoin] from [com.razeware.emitron.model.Content]
     */
    fun listFrom(content: com.razeware.emitron.model.Content): List<GroupEpisodeJoin> =
      content.getContentGroupIds()
        .mapNotNull { id ->
          content.getIncludedContentById(id)
        }.flatMap {
          val groupId = it.id!!
          it.getChildContentIds().map { id ->
            GroupEpisodeJoin(
              groupId = groupId,
              episodeId = id
            )
          }
        }
  }
}
