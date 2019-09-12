package com.raywenderlich.emitron.data.content

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import com.raywenderlich.emitron.data.content.dao.ContentDao
import com.raywenderlich.emitron.data.content.dao.ContentDomainJoinDao
import com.raywenderlich.emitron.data.progressions.dao.ProgressionDao
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.model.DataType
import com.raywenderlich.emitron.model.entity.*
import javax.inject.Inject

/**
 * Local data source to fetch contents
 */
class ContentDataSourceLocal @Inject constructor(
  private val contentDao: ContentDao,
  private val contentDomainJoinDao: ContentDomainJoinDao,
  private val progressionDao: ProgressionDao
) {

  /**
   * Insert contents to db
   */
  fun insertContent(
    dataType: DataType,
    contents: List<Data>
  ) {
    val contentList = Content.listFrom(contents)
    val contentDomainList = ContentDomainJoin.listFrom(contents)

    val progressionList = when (dataType) {
      DataType.Bookmarks -> emptyList()
      else -> {
        val progressions = contents.mapNotNull { it.relationships?.progression }
        Progression.listFrom(progressions)
      }
    }

    contentDao.insertOrUpdateContents(
      contentList,
      progressionList,
      progressionDao,
      contentDomainList,
      contentDomainJoinDao
    )
  }

  /**
   * Get observer for contents table
   */
  fun getContents(): LiveData<List<Content>> = contentDao.getContents()

  /**
   * Get bookmarks DataSource.Factory
   */
  fun getBookmarks(): DataSource.Factory<Int, ContentWithDomain> =
    contentDomainJoinDao.getBookmarks()

  /**
   * Update content bookmark
   *
   * @param contentId Content id
   * @param bookmarkId Bookmark id
   */
  suspend fun updateBookmark(contentId: String, bookmarkId: String?): Unit =
    contentDao.updateBookmark(contentId, bookmarkId)

  /**
   * Get progressions DataSource.Factory
   */
  fun getProgressions(completed: Boolean):
      DataSource.Factory<Int, ContentWithDomainAndProgression> =
    contentDomainJoinDao.getProgressions(completed)

  /**
   * Update content progress
   *
   * @param contentId Content id
   * @param finished Content finished
   */
  suspend fun updateProgress(contentId: String, finished: Boolean): Unit =
    progressionDao.updateProgress(contentId, finished)

}
