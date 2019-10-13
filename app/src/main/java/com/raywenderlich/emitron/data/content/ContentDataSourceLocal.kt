package com.raywenderlich.emitron.data.content

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import com.raywenderlich.emitron.data.content.dao.*
import com.raywenderlich.emitron.data.filter.dao.CategoryDao
import com.raywenderlich.emitron.data.filter.dao.DomainDao
import com.raywenderlich.emitron.data.progressions.dao.ProgressionDao
import com.raywenderlich.emitron.model.ContentType
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.model.DataType
import com.raywenderlich.emitron.model.DownloadState
import com.raywenderlich.emitron.model.entity.*
import org.threeten.bp.Clock
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
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
   * Insert a screencast or video course to db
   */
  fun insertContent(content: com.raywenderlich.emitron.model.Content) {

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
  suspend fun getContent(id: String): ContentDetail = contentDao.getContentDetail(id)

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
  suspend fun updateBookmark(contentId: String, bookmarkId: String?): Unit =
    contentDao.updateBookmark(contentId, bookmarkId)

  /**
   * Get progressions DataSource.Factory
   */
  fun getProgressions(completed: Boolean):
      DataSource.Factory<Int, ContentWithDomainAndProgression> =
    contentDao.getProgressions(completed, ContentType.getAllowedContentTypes())

  /**
   * Update content progress
   *
   * @param contentId Content id
   * @param finished Content finished
   */
  suspend fun updateProgress(contentId: String, finished: Boolean): Unit =
    progressionDao.updateProgress(contentId, finished)

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

  /**
   * Update content download url
   *
   * @param contentId Content id
   * @param url Download url
   */
  fun updateDownloadUrl(
    contentId: String,
    url: String
  ): Unit = downloadDao.updateUrl(contentId, url, DownloadState.IN_PROGRESS.ordinal)

  /**
   * Update download progress
   *
   * @param contentId Content id
   * @param progress Int
   * @param state Download state [DownloadState]
   */
  suspend fun updateDownloadProgress(
    contentId: String,
    progress: Int,
    state: DownloadState
  ): Unit =
    downloadDao.updateProgress(contentId, progress, state.ordinal)

  /**
   * Update download state
   *
   * @param contentId Content id
   * @param state Download state [DownloadState]
   */
  fun updateDownloadState(
    contentId: String,
    state: DownloadState
  ): Unit =
    downloadDao.updateState(contentId, state.ordinal)

  /**
   * Add new download
   *
   * @param contentId Content id
   * @param state Download state
   * @param createdAt LocalDateTime
   */
  suspend fun insertDownload(
    contentId: String,
    state: DownloadState,
    createdAt: LocalDateTime = LocalDateTime.now(Clock.systemUTC())
  ) {
    val download = Download(
      contentId,
      state = state.ordinal,
      createdAt = createdAt.format(DateTimeFormatter.ISO_DATE_TIME)
    )
    downloadDao.insert(download)
  }

  /**
   * Remove a download
   *
   * @param contentId Download id
   */
  suspend fun deleteDownload(
    contentId: String
  ) {
    val download = Download(
      contentId
    )
    downloadDao.delete(download)
  }

  /**
   * Delete all downloads
   */
  suspend fun deleteAllDownloads() {
    downloadDao.deleteAll()
  }

  /**
   * Get queued downloads
   *
   * @param limit no. of pending downloads to fetch
   * @param states download states
   * @param contentTypes download contentTypes
   */
  suspend fun getQueuedDownloads(
    limit: Int = 1,
    states: Array<Int>,
    contentTypes: Array<String>
  ): List<DownloadWithContent> =
    downloadDao.getQueuedDownloads(limit, states, contentTypes)

  /**
   * Get queued download by id
   *
   * @param downloadId Download id
   */
  suspend fun getQueuedDownload(downloadId: String): DownloadWithContent? =
    downloadDao.getQueuedDownload(downloadId)

  /**
   * Get queued downloads factory
   */
  fun getQueuedDownloads(): DataSource.Factory<Int, DownloadWithContent> =
    downloadDao.getQueuedDownloads(ContentType.getAllowedContentTypes())

  /**
   * Get downloads by id
   *
   * @param ids Download ids
   */
  fun getDownloadsById(ids: List<String>): LiveData<List<Download>> =
    downloadDao.getDownloadsById(ids)
}
