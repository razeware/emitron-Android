package com.razeware.emitron.data.progressions.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.Query
import androidx.room.Transaction
import com.razeware.emitron.model.entity.WatchStat

/**
 * Dao for Watch stats
 */
@Dao
interface WatchStatDao {

  /**
   * Insert watch stat
   */
  @Insert(onConflict = IGNORE)
  suspend fun insertWatchStat(watchStat: WatchStat): Long

  /**
   * Delete all watch stats
   */
  @Query("DELETE from watch_stats")
  suspend fun deleteAll()

  /**
   * Get all
   */
  @Query("SELECT * from watch_stats")
  suspend fun getAll(): List<WatchStat>

  /**
   * Find a watch stat by watched at
   */
  @Query(
    """
          SELECT * FROM watch_stats 
          WHERE watched_at = :watchedAt
          """
  )
  suspend fun getWatchStat(watchedAt: String): WatchStat?

  /**
   * Update watch state
   */
  @Query(
    """
          UPDATE 
            watch_stats 
          SET 
            duration = :duration,
            updated_at = :updatedAt
          WHERE 
            watched_at = :watchedAt
          """
  )
  suspend fun updateWatchStat(
    duration: Long,
    watchedAt: String,
    updatedAt: String
  ): Int

  /**
   * Insert or update watch stat
   */
  @Transaction
  suspend fun insertOrUpdateWatchStat(
    watchStat: WatchStat
  ) {
    val existingWatchStat = getWatchStat(watchStat.watchedAt)

    if (null == existingWatchStat) {
      insertWatchStat(watchStat)
    } else {
      val totalDuration = watchStat.duration + existingWatchStat.duration
      updateWatchStat(totalDuration, watchStat.watchedAt, watchStat.updatedAt)
    }
  }
}
