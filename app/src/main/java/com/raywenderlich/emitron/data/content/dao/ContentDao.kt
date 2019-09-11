package com.raywenderlich.emitron.data.content.dao

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.IGNORE
import com.raywenderlich.emitron.data.progressions.dao.ProgressionDao
import com.raywenderlich.emitron.model.entity.Content
import com.raywenderlich.emitron.model.entity.ContentDomainJoin
import com.raywenderlich.emitron.model.entity.Progression

/**
 * Dao for contents
 */
@Dao
interface ContentDao {

  /**
   * Get observer for contents
   */
  @Query("SELECT * FROM contents")
  fun getContents(): LiveData<List<Content>>

  /**
   * Insert contents
   */
  @Insert(onConflict = IGNORE)
  fun insertContents(contents: List<Content>): List<Long>

  /**
   * Update contents
   */
  @Update(onConflict = IGNORE)
  fun updateContents(contents: List<Content>)

  /**
   * Insert contents, if already exists update it
   */
  @WorkerThread
  @Transaction
  fun insertOrUpdateContents(
    contents: List<Content>,
    progressions: List<Progression>,
    progressionDao: ProgressionDao,
    contentDomainJoins: List<ContentDomainJoin>,
    contentDomainJoinDao: ContentDomainJoinDao
  ) {

    insertOrUpdateProgressions(progressionDao, progressions)
    val insertResult = insertContents(contents)
    val itemsToUpdate = insertResult.mapIndexedNotNull { index, result ->
      if (result == -1L) {
        contents[index]
      } else {
        null
      }
    }

    if (itemsToUpdate.isNotEmpty()) {
      updateContents(itemsToUpdate)
    }

    if (contentDomainJoins.isNotEmpty()) {
      contentDomainJoinDao.insertContentDomainJoin(contentDomainJoins)
    }
  }

  @WorkerThread
  private fun insertOrUpdateProgressions(
    progressionDao: ProgressionDao,
    progressions: List<Progression>
  ) {
    if (progressions.isEmpty()) {
      return
    }

    val progressionInsertResult = progressionDao.insertProgressions(progressions)
    val updateProgressions = progressionInsertResult.mapIndexedNotNull { index, result ->
      if (result == -1L) {
        progressions[index]
      } else {
        null
      }
    }

    if (updateProgressions.isNotEmpty()) {
      progressionDao.updateProgressions(updateProgressions)
    }
  }

  /**
   * Update content bookmark id
   *
   * @param contentId Content id
   * @param bookmarkId Bookmark id
   */
  @Query("UPDATE contents set bookmark_id = :bookmarkId WHERE content_id = :contentId")
  suspend fun updateBookmark(contentId: String, bookmarkId: String?)
}
