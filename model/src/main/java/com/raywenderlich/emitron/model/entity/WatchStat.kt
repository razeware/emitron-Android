package com.raywenderlich.emitron.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity to store contents to database
 */
@Entity(
  tableName = WatchStat.TABLE_NAME,
  indices = [Index("id", unique = true)]
)
data class WatchStat(

  /**
   * Id
   */
  @PrimaryKey(autoGenerate = true)
  val id: Int,

  /**
   * Content id
   */
  @ColumnInfo(name = "content_id")
  val contentId: String,

  /**
   * Duration
   */
  @ColumnInfo(name = "duration")
  val duration: String,

  /**
   * Updated at
   */
  @ColumnInfo(name = "updated_at")
  val updatedAt: String
) {

  companion object {

    /**
     * Table name to store categories
     */
    const val TABLE_NAME: String = "watch_stats"
  }
}
