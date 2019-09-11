package com.raywenderlich.emitron.data.content.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import com.raywenderlich.emitron.model.entity.ContentDomainJoin
import com.raywenderlich.emitron.model.entity.ContentWithDomain
import com.raywenderlich.emitron.model.entity.ContentWithDomainAndProgression

/**
 * Dao for contents
 */
@Dao
interface ContentDomainJoinDao {

  @Insert(onConflict = REPLACE)
  fun insertContentDomainJoin(contents: List<ContentDomainJoin>)

  @Query(
    """
         SELECT * FROM contents
         WHERE contents.bookmark_id IS NOT NULL
         ORDER BY contents.bookmark_id DESC
         """
  )
  @Transaction
  fun getBookmarks(): DataSource.Factory<Int, ContentWithDomain>

  @Query(
    """
          SELECT * FROM contents 
          INNER JOIN progressions
          ON progressions.progression_id = contents.progression_id
          WHERE progressions.finished = :completed
          ORDER BY progression_id DESC
          """
  )
  @Transaction
  fun getProgressions(completed: Boolean):
      DataSource.Factory<Int, ContentWithDomainAndProgression>
}
