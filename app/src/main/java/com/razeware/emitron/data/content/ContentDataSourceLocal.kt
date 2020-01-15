package com.razeware.emitron.data.content

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import com.razeware.emitron.data.content.dao.*
import com.razeware.emitron.data.filter.dao.CategoryDao
import com.razeware.emitron.data.filter.dao.DomainDao
import com.razeware.emitron.data.progressions.dao.ProgressionDao
import com.razeware.emitron.model.ContentType
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.DataType
import com.razeware.emitron.model.entity.*
import javax.inject.Inject

/**
 * Local data source to fetch contents
 */
class ContentDataSourceLocal @Inject constructor(
  private val contentDao: ContentDao,
  private val contentDomainJoinDao: ContentDomainJoinDao,
  private val progressionDao: ProgressionDao,
  private val domainDao: DomainDao,
  private val categoryDao: CategoryDao,
  private val groupDao: GroupDao,
  private val contentGroupJoinDao: ContentGroupJoinDao,
  private val groupEpisodeJoinDao: GroupEpisodeJoinDao,
  private val downloadDao: DownloadDao
) {

  /**
   * Insert contents to db
   */
  fun insertContents(
    dataType: DataType,
    contents: List<Data>
  ) {
    val contentList = Content.listFrom(contents)
    val contentDomainList = ContentDomainJoin.listFrom(contents)

    val progressionList = when (dataType) {
      /**
       * In ideal scenario the bookmark response should also include the progressions data,
       * but since it doesn't to avoid overwrites of existing progressions we will avoid updates,
       * so progression list is empty from bookmarks.
       */
      DataType.Bookmarks -> emptyList()
      else -> {
        Progression.listFrom(contents)
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
   * Insert a screencast or video course to db
   */
  suspend fun insertContent(content: com.razeware.emitron.model.Content) {

    val contentData = content.datum ?: return
    val groups = Group.listFrom(content)
    val collection = Content.from(contentData)
    val contentDomainList = ContentDomainJoin.from(contentData)

    val includedData = content.included
    val progressions = if (!includedData.isNullOrEmpty()) {
      Progression.listFromIncluded(includedData)
    } else {
      emptyList()
    }
    val episodes = if (content.isTypeScreencast()) {
      emptyList()
    } else {
      Content.episodesFrom(content)
    }
    val contents = listOf(collection).plus(episodes)

    val contentGroupJoins = ContentGroupJoin.listFrom(
      collection.contentId, groups
    )
    val groupEpisodeJoins = GroupEpisodeJoin.listFrom(content)

    contentDao.insertOrUpdateContent(
      contents,
      progressions,
      progressionDao,
      contentDomainList,
      contentDomainJoinDao,
      groups,
      groupDao,
      contentGroupJoins,
      contentGroupJoinDao,
      groupEpisodeJoins,
      groupEpisodeJoinDao
    )
  }

  /**
   * Get observer for contents table
   */
  fun getContents(): LiveData<List<Content>> = contentDao.getContents()

  /**
   * Get a content (screencast/video-course)
   *
   * @param id Id of the content
   *
   * @return [ContentDetail]
   */
  suspend fun getContent(id: String): ContentDetail? = contentDao.getContentDetail(id)

  /**
   * Get bookmarks DataSource.Factory
   */
  fun getBookmarks(): DataSource.Factory<Int, ContentWithDomain> =
    contentDao.getBookmarks(ContentType.getAllowedContentTypes())

  /**
   * Update content bookmark
   *
   * @param contentId Content id
   * @param bookmarkId Bookmark id
   */
  suspend fun updateBookmark(contentId: String, bookmarkId: String?) {
    contentDao.updateBookmark(contentId, bookmarkId)
  }

  /**
   * Get progressions DataSource.Factory
   */
  fun getProgressions(completed: Boolean):
      DataSource.Factory<Int, ContentWithDomainAndProgression> =
    contentDao.getProgressions(completed, ContentType.getAllowedContentTypes())

  /**
   * Delete all tables
   */
  suspend fun deleteAll() {
    contentDao.deleteAll(
      domainDao,
      categoryDao,
      contentDomainJoinDao,
      progressionDao,
      groupDao,
      contentGroupJoinDao,
      groupEpisodeJoinDao,
      downloadDao
    )
  }
}
