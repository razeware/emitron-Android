package com.razeware.emitron.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.razeware.emitron.model.*
import com.razeware.emitron.model.Content

/**
 * Entity to store groups to database
 */
@Entity(
  tableName = Group.TABLE_NAME
)
data class Group(

  /**
   * Group id [Data.id]
   */
  @PrimaryKey
  @ColumnInfo(name = "group_id")
  val groupId: String,

  /**
   * Group name [Data.getName]
   */
  val name: String?,

  /**
   * Group ordinal [Data.getOrdinal]
   */
  var ordinal: Int = 0
) {

  /**
   * @return [Data] from [Group]
   */
  fun toData(): Data = Data(
    id = groupId,
    type = DataType.Groups.toRequestFormat(),
    attributes = Attributes(
      name = name,
      ordinal = ordinal
    )
  )

  companion object {

    /**
     * Table name for groups
     */
    const val TABLE_NAME: String = "groups"

    /**
     * Create list of [Group] from list of [Data]
     *
     * @return list of [Group]
     */
    fun listFrom(content: Content): List<Group> =
      content.getContentGroupIds()
        .mapNotNull { id ->
          val group = content.getIncludedContentById(id)
          group?.let {
            Group(
              groupId = it.id!!,
              name = it.getName(),
              ordinal = it.getOrdinal()
            )
          }
        }
  }
}
