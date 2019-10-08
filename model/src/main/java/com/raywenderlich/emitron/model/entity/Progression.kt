package com.raywenderlich.emitron.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.raywenderlich.emitron.model.*
import com.raywenderlich.emitron.model.Content

/**
 * Entity to store contents to database
 */
@Entity(
  tableName = Progression.TABLE_NAME,
  indices = [Index("progression_id", unique = true)]
)
data class Progression(

  /**
   * Progression server id
   */
  @PrimaryKey
  @ColumnInfo(name = "progression_id")
  val progressionId: String,

  /**
   * Percent completion (0-100)%
   */
  @ColumnInfo(name = "percent_complete")
  val percentComplete: Int?,

  /**
   * Is content finished
   */
  val finished: Boolean
) {

  /**
   * Build [Data] from [Progression]
   */
  fun toData(): Data = Data(
    id = progressionId,
    type = DataType.Progressions.toRequestFormat(),
    attributes = Attributes(
      percentComplete = percentComplete?.toDouble(),
      finished = finished
    )
  )

  companion object {

    /**
     * Table name to store categories
     */
    const val TABLE_NAME: String = "progressions"

    /**
     * Create list of [Domain] from list of [Data]
     *
     * @return list of [Domain]
     */
    fun listFrom(progressions: List<Content>): List<Progression> =
      progressions.map {
        Progression(
          progressionId = it.getChildId() ?: "",
          percentComplete = it.getPercentComplete(),
          finished = it.isFinished()
        )
      }
  }
}
