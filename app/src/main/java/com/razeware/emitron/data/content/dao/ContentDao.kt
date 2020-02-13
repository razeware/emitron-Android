package com.razeware.emitron.data.content.dao

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import androidx.room.OnConflictStrategy.IGNORE
import com.razeware.emitron.data.filter.dao.CategoryDao
import com.razeware.emitron.data.filter.dao.DomainDao
import com.razeware.emitron.data.progressions.dao.ProgressionDao
import com.razeware.emitron.model.ContentType
import com.razeware.emitron.model.entity.*

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

  /**
   * Insert content, if already exists update it
   */
  @WorkerThread
  @Transaction
  suspend fun insertOrUpdateContent(
    contents: List<Content>,
    progressions: List<Progression>,
    progressionDao: ProgressionDao,
    contentDomainJoins: List<ContentDomainJoin>,
    contentDomainJoinDao: ContentDomainJoinDao,
    groups: List<Group>,
    groupDao: GroupDao,
    contentGroupJoins: List<ContentGroupJoin>,
    contentGroupJoinDao: ContentGroupJoinDao,
    groupEpisodeJoins: List<GroupEpisodeJoin>,
    groupEpisodeJoinDao: GroupEpisodeJoinDao
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

    if (groups.isNotEmpty()) {
      groupDao.insertGroups(groups)
    }

    if (contentGroupJoins.isNotEmpty()) {
      contentGroupJoinDao.insertContentGroupJoin(contentGroupJoins)
    }

    if (groupEpisodeJoins.isNotEmpty()) {
      groupEpisodeJoinDao.insertGroupEpisodeJoin(groupEpisodeJoins)
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

  /**
   * Get bookmarks
   *
   * @param contentTypes [ContentType]s to filter on
   */
  @Query(
    """
         SELECT * FROM contents
         WHERE contents.bookmark_id IS NOT NULL
         AND contents.content_type in(:contentTypes)
         ORDER BY contents.bookmark_id DESC
         """
  )
  @Transaction
  fun getBookmarks(contentTypes: Array<String>): DataSource.Factory<Int, ContentWithDomain>

  /**
   * Get progressions
   *
   * @param contentTypes [ContentType]s to filter on
   */
  @Query(
    """
          SELECT * FROM contents 
          INNER JOIN progressions
          ON progressions.content_id = contents.content_id
          WHERE progressions.finished = :completed
          AND contents.content_type in(:contentTypes)
          ORDER BY datetime(progressions.updated_at) DESC
          """
  )
  @Transaction
  fun getProgressions(completed: Boolean, contentTypes: Array<String>):
      DataSource.Factory<Int, ContentWithDomainAndProgression>


  /**
   * Load collection (video course/screencast)
   *
   * @param id content id
   */
  @Query(
    """
          SELECT * FROM contents 
          WHERE content_id = :id
          """
  )
  @Transaction
  suspend fun getContentDetail(id: String): ContentDetail?

  /**
   * Delete contents tables
   */
  @Query("DELETE from contents")
  fun deleteAll()

  /**
   * Delete all tables
   */
  @WorkerThread
  @Transaction
  suspend fun deleteAll(
    domainDao: DomainDao,
    categoryDao: CategoryDao,
    contentDomainJoinDao: ContentDomainJoinDao,
    progressionDao: ProgressionDao,
    groupDao: GroupDao,
    contentGroupJoinDao: ContentGroupJoinDao,
    groupEpisodeJoinDao: GroupEpisodeJoinDao,
    downloadDao: DownloadDao
  ) {
    contentDomainJoinDao.deleteAll()
    domainDao.deleteAll()
    categoryDao.deleteAll()
    progressionDao.deleteAll()
    groupDao.deleteAll()
    contentGroupJoinDao.deleteAll()
    groupEpisodeJoinDao.deleteAll()
    downloadDao.deleteAll()
    deleteAll()
  }
}
