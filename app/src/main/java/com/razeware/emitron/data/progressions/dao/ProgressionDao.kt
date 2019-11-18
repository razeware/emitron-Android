package com.razeware.emitron.data.progressions.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.IGNORE
import com.razeware.emitron.data.content.dao.ContentDao
import com.razeware.emitron.model.entity.Progression

/**
 * Dao for progressions
 */
@Dao
interface ProgressionDao {

  /**
   * Insert progressions
   */
  @Insert(onConflict = IGNORE)
  fun insertProgressions(contents: List<Progression>): List<Long>

  /**
   * Insert progression
   */
  @Insert(onConflict = IGNORE)
  fun insertProgression(progression: Progression): Long

  /**
   * Update progressions
   */
  @Update(onConflict = IGNORE)
  fun updateProgressions(contents: List<Progression>)

  /**
   * Update progressions (async)
   */
  @Update(onConflict = IGNORE)
  suspend fun updateProgressionsAsync(contents: List<Progression>)

  /**
   * Update progressions
   */
  @Query(
    """
          UPDATE 
            progressions 
          SET 
            progress = :progress,
            percent_complete = :percentComplete,
            finished = :finished,
            synced = :synced,
            updated_at = :updatedAt,
            progression_id = :progressionId
          WHERE 
            content_id = :contentId
          """
  )
  suspend fun updateProgress(
    contentId: String,
    percentComplete: Int,
    progress: Long,
    finished: Boolean,
    synced: Boolean,
    updatedAt: String,
    progressionId: String?
  ): Int

  /**
   * Insert progression or update if it already exists.
   */
  @Transaction
  suspend fun insertOrUpdateProgress(
    contentId: String,
    percentComplete: Int,
    progress: Long,
    finished: Boolean,
    synced: Boolean,
    updatedAt: String,
    progressionId: String?,
    contentDao: ContentDao
  ) {
    val updatedRows =
      updateProgress(
        contentId,
        percentComplete,
        progress,
        finished,
        synced,
        updatedAt,
        progressionId
      )
    if (updatedRows == 0) {
      val progression = Progression(
        contentId = contentId,
        percentComplete = percentComplete,
        progress = progress,
        finished = finished,
        updatedAt = updatedAt,
        synced = synced
      )
      insertProgression(progression)
    }
  }

  /**
   * Get progression by content id
   *
   * @param contentId content id
   */
  @Query(
    """
          SELECT * FROM progressions 
          WHERE content_id = :contentId
          """
  )
  suspend fun getProgression(contentId: String): Progression

  /**
   * Get progression by content id
   *
   */
  @Query(
    """
          SELECT * FROM progressions 
          WHERE synced = 0
          """
  )
  suspend fun getLocalProgressions(): List<Progression>

  /**
   * Delete all progressions
   */
  @Query("DELETE from progressions")
  fun deleteAll()
}
