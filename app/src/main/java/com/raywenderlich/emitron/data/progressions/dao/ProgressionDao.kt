package com.raywenderlich.emitron.data.progressions.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.Query
import androidx.room.Update
import com.raywenderlich.emitron.model.entity.Progression

/**
 * Dao for progressions
 */
@Dao
interface ProgressionDao {

  @Insert(onConflict = IGNORE)
  fun insertProgressions(contents: List<Progression>): List<Long>

  @Update(onConflict = IGNORE)
  fun updateProgressions(contents: List<Progression>)

  @Query("UPDATE progressions set finished = :finished WHERE progression_id = :progressionId")
  suspend fun updateProgress(progressionId: String, finished: Boolean)
}
