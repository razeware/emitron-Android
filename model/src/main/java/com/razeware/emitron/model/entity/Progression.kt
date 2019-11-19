package com.razeware.emitron.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.razeware.emitron.model.*
import com.razeware.emitron.model.Content

/**
 * Entity to store contents to database
 */
@Entity(
  tableName = Progression.TABLE_NAME,
  indices = [Index("id", unique = true),
    Index("progression_id", unique = true), Index("content_id", unique = true)]
)
data class Progression(


  /**
   * Progression server id
   */
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  val id: Int = 0,

  /**
   * Progression server id
   */
  @ColumnInfo(name = "content_id")
  val contentId: String,

  /**
   * Progression server id
   */
  @ColumnInfo(name = "progression_id")
  val progressionId: String? = null,

  /**
   * Percent completion (0-100)%
   */
  @ColumnInfo(name = "percent_complete")
  val percentComplete: Int = 0,

  /**
   * Duration
   */
  @ColumnInfo(name = "progress")
  val progress: Long = 0,

  /**
   * Is content finished
   */
  val finished: Boolean,

  /**
   * Updated at
   */
  @ColumnInfo(name = "updated_at")
  val updatedAt: String? = null,

  /**
   * Synced
   */
  val synced: Boolean = false
) {

  /**
   * Build [Data] from [Progression]
   */
  fun toData(): Data = Data(
    id = progressionId,
    type = DataType.Progressions.toRequestFormat(),
    attributes = Attributes(
      contentId = contentId,
      percentComplete = percentComplete.toDouble(),
      progress = progress,
      finished = finished,
      updatedAt = updatedAt
    ),
    relationships = Relationships(
      content = Content(
        datum = Data(id = contentId)
      )
    )
  )

  companion object {

    /**
     * Table name to store categories
     */
    const val TABLE_NAME: String = "progressions"

    /**
     * Create list of [Progression] from list of [Data]
     *
     * @return list of [Progression]
     */
    fun listFrom(contents: List<Data>): List<Progression> =
      contents.mapNotNull { content ->
        content.relationships?.progression?.toProgression()
      }

    /**
     * Create list of [Progression] from list of [Data]
     *
     * @return list of [Progression]
     */
    fun listFromIncluded(progressions: List<Data>): List<Progression> =
      progressions.filter {
        it.isTypeProgression()
      }.map {
        it.toProgression(it.getContentId())
      }
  }
}
