package com.razeware.emitron.data.content.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.razeware.emitron.model.entity.ContentGroupJoin

/**
 * Dao for contents
 */
@Dao
interface ContentGroupJoinDao {

  /**
   * Insert content group join
   */
  @Insert(onConflict = REPLACE)
  suspend fun insertContentGroupJoin(contents: List<ContentGroupJoin>)

  /**
   * Delete all content group joins
   */
  @Query("DELETE from content_group_join")
  suspend fun deleteAll()
}
