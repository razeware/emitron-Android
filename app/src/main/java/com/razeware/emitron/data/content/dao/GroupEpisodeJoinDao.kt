package com.razeware.emitron.data.content.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.razeware.emitron.model.entity.GroupEpisodeJoin

/**
 * Dao for collection episode group to episode join
 */
@Dao
interface GroupEpisodeJoinDao {

  /**
   * Insert collection episode group to episode (content) join
   */
  @Insert(onConflict = REPLACE)
  suspend fun insertGroupEpisodeJoin(contents: List<GroupEpisodeJoin>)

  /**
   * Delete all group episode joins
   */
  @Query("DELETE from group_content_join")
  suspend fun deleteAll()
}
