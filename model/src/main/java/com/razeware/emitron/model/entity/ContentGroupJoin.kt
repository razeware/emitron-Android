package com.razeware.emitron.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
  tableName = ContentGroupJoin.TABLE_NAME,
  indices = [Index("content_id"), Index("group_id")],
  primaryKeys = ["content_id", "group_id"],
  foreignKeys = [ForeignKey(
    entity = Content::class,
    parentColumns = arrayOf("content_id"),
    childColumns = arrayOf("content_id")
  ), ForeignKey(
    entity = Group::class,
    parentColumns = arrayOf("group_id"),
    childColumns = arrayOf("group_id")
  )]
)
/**
 * Entity for content-group relations
 */
data class ContentGroupJoin(
  /**
   * Content Id
   */
  @ColumnInfo(name = "content_id")
  val contentId: String,
  /**
   * Group Id
   */
  @ColumnInfo(name = "group_id")
  val groupId: String
) {
  companion object {

    /**
     * Table for content-group relations
     */
    const val TABLE_NAME: String = "content_group_join"

    /**
     * List of [ContentGroupJoin] from contentId, and groups
     *
     * @param contentId
     * @param groups list of groups
     *
     * @return list of [Group]
     */
    fun listFrom(contentId: String, groups: List<Group>): List<ContentGroupJoin> =
      groups.map { (id) ->
        ContentGroupJoin(
          contentId = contentId,
          groupId = id
        )
      }
  }
}
