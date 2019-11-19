package com.razeware.emitron.data.content.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.razeware.emitron.model.entity.ContentDomainJoin

/**
 * Dao for contents
 */
@Dao
interface ContentDomainJoinDao {

  /**
   * Insert content domain join
   */
  @Insert(onConflict = REPLACE)
  fun insertContentDomainJoin(contents: List<ContentDomainJoin>)

  /**
   * Delete all content domain joins
   */
  @Query("DELETE from content_domain_join")
  suspend fun deleteAll()
}
