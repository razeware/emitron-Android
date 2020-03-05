package com.razeware.emitron.data.content.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.Query
import com.razeware.emitron.model.entity.Group

/**
 * Dao for progressions
 */
@Dao
interface GroupDao {

  /**
   * Insert collection episode groups
   */
  @Insert(onConflict = IGNORE)
  suspend fun insertGroups(contents: List<Group>): List<Long>

  /**
   * Delete all collection episode groups
   */
  @Query("DELETE from groups")
  suspend fun deleteAll()
}
