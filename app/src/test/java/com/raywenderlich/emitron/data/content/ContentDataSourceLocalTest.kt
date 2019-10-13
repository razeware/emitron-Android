package com.raywenderlich.emitron.data.content

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.*
import com.raywenderlich.emitron.data.content.dao.*
import com.raywenderlich.emitron.data.filter.dao.CategoryDao
import com.raywenderlich.emitron.data.filter.dao.DomainDao
import com.raywenderlich.emitron.data.progressions.dao.ProgressionDao
import com.raywenderlich.emitron.model.*
import com.raywenderlich.emitron.model.Content
import com.raywenderlich.emitron.model.entity.*
import com.raywenderlich.emitron.model.entity.Download
import com.raywenderlich.emitron.utils.TestCoroutineRule
import com.raywenderlich.emitron.utils.isEqualTo
import com.raywenderlich.emitron.utils.observeForTestingResultNullable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Month
import org.threeten.bp.format.DateTimeFormatter

class ContentDataSourceLocalTest {

  private val contentDao: ContentDao = mock()
  private val contentDomainJoinDao: ContentDomainJoinDao = mock()
  private val progressionDao: ProgressionDao = mock()
  private val domainDao: DomainDao = mock()
  private val categoryDao: CategoryDao = mock()
  private val groupDao: GroupDao = mock()
  private val contentGroupJoinDao: ContentGroupJoinDao = mock()
  private val groupEpisodeJoinDao: GroupEpisodeJoinDao = mock()
  private val downloadDao: DownloadDao = mock()

  private lateinit var contentDataSourceLocal: ContentDataSourceLocal

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  @Before
  fun setUp() {
    contentDataSourceLocal =
      ContentDataSourceLocal(
        contentDao,
        contentDomainJoinDao,
        progressionDao,
        domainDao,
        categoryDao,
        groupDao,
        contentGroupJoinDao,
        groupEpisodeJoinDao,
        downloadDao
      )
  }

  @Test
  fun insertContents() {
    contentDataSourceLocal.insertContents(
      DataType.Bookmarks,
      listOf(
        Data(
          id = "1",
          type = "contents",
          attributes = Attributes(
            createdAt = "",
            description = "In this tutorial you will learn how to use lambda.",
            level = "",
            name = "Introduction to Kotlin Lambdas: Getting Started",
            slug = "",
            cardArtworkUrl = "https://koenig-media.raywenderlich.com/",
            contentType = "screencast",
            difficulty = "beginner",
            duration = 408,
            free = false,
            popularity = 0.0,
            releasedAt = "2019-08-08T00:00:00.000Z",
            uri = "",
            target = 0,
            progress = 0,
            finished = false,
            percentComplete = 0.0,
            updatedAt = "",
            technology = "Swift, iOS",
            contributors = "Luke",
            url = "https://koenig-media.raywenderlich.com/",
            kind = ""
          ),
          links = null,
          relationships = Relationships(
            content = null,
            contents = null,
            bookmark = Content(
              datum = Data(
                id = "1",
                type = "bookmarks",
                attributes = null,
                links = null,
                relationships = null,
                meta = null,
                included = null
              ), links = null, meta = null, included = null
            ),
            domains = Contents(
              datum = listOf(
                Data(
                  id = "1",
                  type = "domains",
                  attributes = Attributes(name = "iOS & Swift", level = null)
                )
              )
            ),
            progression = Content(
              datum = Data(
                id = "1",
                type = "progressions",
                attributes = Attributes(percentComplete = 99.0, finished = true),
                links = null,
                relationships = null,
                meta = null,
                included = null
              ), links = null, meta = null, included = null
            ),
            groups = null,
            childContents = null
          )
        ),
        Data(
          id = "2",
          type = "contents",
          attributes = Attributes(
            createdAt = "",
            description = "In this tutorial you will learn how to use lambda.",
            level = "",
            name = "Introduction to Kotlin Lambdas: Getting Started",
            slug = "",
            cardArtworkUrl = "https://koenig-media.raywenderlich.com/",
            contentType = "screencast",
            difficulty = "beginner",
            duration = 408,
            free = false,
            popularity = 0.0,
            releasedAt = "2019-08-08T00:00:00.000Z",
            uri = "",
            target = 0,
            progress = 0,
            finished = false,
            percentComplete = 0.0,
            updatedAt = "",
            technology = "Swift, iOS",
            contributors = "Luke",
            url = "https://koenig-media.raywenderlich.com/",
            kind = ""
          ),
          links = null,
          relationships = Relationships(
            content = null,
            contents = null,
            bookmark = Content(
              datum = Data(
                id = "2",
                type = "bookmarks",
                attributes = null,
                links = null,
                relationships = null,
                meta = null,
                included = null
              ), links = null, meta = null, included = null
            ),
            domains = Contents(
              datum = listOf(
                Data(
                  id = "2",
                  type = "domains",
                  attributes = Attributes(name = "Android & Kotlin", level = null)
                )
              )
            ),
            progression = Content(
              datum = Data(
                id = "2",
                type = "progressions",
                attributes = Attributes(percentComplete = 50.0, finished = false),
                links = null,
                relationships = null,
                meta = null,
                included = null
              ), links = null, meta = null, included = null
            ),
            groups = null,
            childContents = null
          )
        )
      )
    )

    verify(contentDao)
      .insertOrUpdateContents(
        listOf(
          com.raywenderlich.emitron.model.entity.Content(
            contentId = "1",
            name = "Introduction to Kotlin Lambdas: Getting Started",
            description = "In this tutorial you will learn how to use lambda.",
            contributors = "Luke",
            professional = true,
            deleted = false,
            contentType = "screencast",
            difficulty = "beginner",
            releasedAt = "2019-08-08T00:00:00.000Z",
            technology = "Swift, iOS",
            duration = 408,
            streamUrl = "",
            cardArtworkUrl = "https://koenig-media.raywenderlich.com/",
            videoId = null,
            bookmarkId = "1",
            progressionId = "1",
            updatedAt = ""
          ),
          com.raywenderlich.emitron.model.entity.Content(
            contentId = "2",
            name = "Introduction to Kotlin Lambdas: Getting Started",
            description = "In this tutorial you will learn how to use lambda.",
            contributors = "Luke",
            professional = true,
            deleted = false,
            contentType = "screencast",
            difficulty = "beginner",
            releasedAt = "2019-08-08T00:00:00.000Z",
            technology = "Swift, iOS",
            duration = 408,
            streamUrl = "",
            cardArtworkUrl = "https://koenig-media.raywenderlich.com/",
            videoId = null,
            bookmarkId = "2",
            progressionId = "2",
            updatedAt = ""
          )
        ),
        emptyList(),
        progressionDao,
        listOf(
          ContentDomainJoin("1", "1"),
          ContentDomainJoin("2", "2")
        ),
        contentDomainJoinDao
      )
    verifyNoMoreInteractions(contentDao)
  }

  @Test
  fun insertContent_fromProgressions() {
    contentDataSourceLocal.insertContents(
      DataType.Progressions,
      listOf(
        Data(
          id = "1",
          type = "contents",
          attributes = Attributes(
            createdAt = "",
            description = "In this tutorial you will learn how to use lambda.",
            level = "",
            name = "Introduction to Kotlin Lambdas: Getting Started",
            slug = "",
            cardArtworkUrl = "https://koenig-media.raywenderlich.com/",
            contentType = "screencast",
            difficulty = "beginner",
            duration = 408,
            free = false,
            popularity = 0.0,
            releasedAt = "2019-08-08T00:00:00.000Z",
            uri = "",
            target = 0,
            progress = 0,
            finished = false,
            percentComplete = 0.0,
            updatedAt = "",
            technology = "Swift, iOS",
            contributors = "Luke",
            url = "https://koenig-media.raywenderlich.com/",
            kind = ""
          ),
          links = null,
          relationships = Relationships(
            content = null,
            contents = null,
            bookmark = Content(
              datum = Data(
                id = "1",
                type = "bookmarks",
                attributes = null,
                links = null,
                relationships = null,
                meta = null,
                included = null
              ), links = null, meta = null, included = null
            ),
            domains = Contents(
              datum = listOf(
                Data(
                  id = "1",
                  type = "domains",
                  attributes = Attributes(name = "iOS & Swift", level = null)
                )
              )
            ),
            progression = Content(
              datum = Data(
                id = "1",
                type = "progressions",
                attributes = Attributes(percentComplete = 99.0, finished = true),
                links = null,
                relationships = null,
                meta = null,
                included = null
              ), links = null, meta = null, included = null
            ),
            groups = null,
            childContents = null
          )
        ),
        Data(
          id = "2",
          type = "contents",
          attributes = Attributes(
            createdAt = "",
            description = "In this tutorial you will learn how to use lambda.",
            level = "",
            name = "Introduction to Kotlin Lambdas: Getting Started",
            slug = "",
            cardArtworkUrl = "https://koenig-media.raywenderlich.com/",
            contentType = "screencast",
            difficulty = "beginner",
            duration = 408,
            free = false,
            popularity = 0.0,
            releasedAt = "2019-08-08T00:00:00.000Z",
            uri = "",
            target = 0,
            progress = 0,
            finished = false,
            percentComplete = 0.0,
            updatedAt = "",
            technology = "Swift, iOS",
            contributors = "Luke",
            url = "https://koenig-media.raywenderlich.com/",
            kind = ""
          ),
          links = null,
          relationships = Relationships(
            content = null,
            contents = null,
            bookmark = Content(
              datum = Data(
                id = "2",
                type = "bookmarks",
                attributes = null,
                links = null,
                relationships = null,
                meta = null,
                included = null
              ), links = null, meta = null, included = null
            ),
            domains = Contents(
              datum = listOf(
                Data(
                  id = "2",
                  type = "domains",
                  attributes = Attributes(name = "Android & Kotlin", level = null)
                )
              )
            ),
            progression = Content(
              datum = Data(
                id = "2",
                type = "progressions",
                attributes = Attributes(percentComplete = 50.0, finished = false),
                links = null,
                relationships = null,
                meta = null,
                included = null
              ), links = null, meta = null, included = null
            ),
            groups = null,
            childContents = null
          )
        )
      )
    )

    verify(contentDao)
      .insertOrUpdateContents(
        listOf(
          com.raywenderlich.emitron.model.entity.Content(
            contentId = "1",
            name = "Introduction to Kotlin Lambdas: Getting Started",
            description = "In this tutorial you will learn how to use lambda.",
            contributors = "Luke",
            professional = true,
            deleted = false,
            contentType = "screencast",
            difficulty = "beginner",
            releasedAt = "2019-08-08T00:00:00.000Z",
            technology = "Swift, iOS",
            duration = 408,
            streamUrl = "",
            cardArtworkUrl = "https://koenig-media.raywenderlich.com/",
            videoId = null,
            bookmarkId = "1",
            progressionId = "1",
            updatedAt = ""
          ),
          com.raywenderlich.emitron.model.entity.Content(
            contentId = "2",
            name = "Introduction to Kotlin Lambdas: Getting Started",
            description = "In this tutorial you will learn how to use lambda.",
            contributors = "Luke",
            professional = true,
            deleted = false,
            contentType = "screencast",
            difficulty = "beginner",
            releasedAt = "2019-08-08T00:00:00.000Z",
            technology = "Swift, iOS",
            duration = 408,
            streamUrl = "",
            cardArtworkUrl = "https://koenig-media.raywenderlich.com/",
            videoId = null,
            bookmarkId = "2",
            progressionId = "2",
            updatedAt = ""
          )
        ),
        listOf(
          Progression(progressionId = "1", percentComplete = 99, finished = true),
          Progression(progressionId = "2", percentComplete = 50, finished = false)
        ),
        progressionDao,
        listOf(
          ContentDomainJoin("1", "1"),
          ContentDomainJoin("2", "2")
        ),
        contentDomainJoinDao
      )
    verifyNoMoreInteractions(contentDao)
  }

  @Test
  fun getContents() {
    val content = com.raywenderlich.emitron.model.entity.Content(
      contentId = "1",
      name = "Introduction to Kotlin Lambdas: Getting Started",
      description = "In this tutorial you will learn how to use lambda.",
      contributors = "Luke",
      professional = false,
      deleted = false,
      contentType = "screencast",
      difficulty = "beginner",
      releasedAt = "2019-08-08T00:00:00.000Z",
      technology = "Swift, iOS",
      duration = 408,
      streamUrl = "https://koenig-media.raywenderlich.com/",
      cardArtworkUrl = "https://koenig-media.raywenderlich.com/",
      videoId = "1",
      bookmarkId = "1",
      progressionId = "1",
      updatedAt = "2019-08-08T00:00:00.000Z"
    )
    val contents = MutableLiveData<List<com.raywenderlich.emitron.model.entity.Content>>().apply {
      value = listOf(content)
    }
    whenever(contentDataSourceLocal.getContents()).doReturn(contents)

    val result =
      contentDataSourceLocal.getContents().observeForTestingResultNullable()
    result isEqualTo listOf(content)
  }

  @Test
  fun getBookmarks() {
    contentDao.getBookmarks(arrayOf())
    verify(contentDao).getBookmarks(arrayOf())
    verifyNoMoreInteractions(contentDomainJoinDao)
  }

  @Test
  fun updateBookmark() {
    testCoroutineRule.runBlockingTest {
      contentDataSourceLocal.updateBookmark("1", "2")
      verify(contentDao).updateBookmark("1", bookmarkId = "2")
      verifyNoMoreInteractions(contentDao)
    }
  }

  @Test
  fun getProgressions() {
    contentDataSourceLocal.getProgressions(true)
    verify(contentDao).getProgressions(true, arrayOf("collection", "screencast"))
    verifyNoMoreInteractions(contentDomainJoinDao)
  }

  @Test
  fun updateProgress() {
    testCoroutineRule.runBlockingTest {
      contentDataSourceLocal.updateProgress("1", true)
      verify(progressionDao).updateProgress("1", true)
      verifyNoMoreInteractions(progressionDao)
    }
  }

  @Test
  fun deleteAll() {
    testCoroutineRule.runBlockingTest {
      contentDataSourceLocal.deleteAll()
      verify(contentDao).deleteAll(
        domainDao,
        categoryDao,
        contentDomainJoinDao,
        progressionDao,
        groupDao,
        contentGroupJoinDao,
        groupEpisodeJoinDao,
        downloadDao
      )
      verifyNoMoreInteractions(contentDao)
    }
  }

  @Test
  fun updateDownloadUrl() {
    testCoroutineRule.runBlockingTest {
      contentDataSourceLocal.updateDownloadUrl("1", "download/1")
      verify(downloadDao).updateUrl("1", "download/1", 2)
      verifyNoMoreInteractions(downloadDao)
    }
  }

  @Test
  fun updateDownloadProgress() {
    testCoroutineRule.runBlockingTest {
      contentDataSourceLocal.updateDownloadProgress(
        "1", 25,
        DownloadState.COMPLETED
      )
      verify(downloadDao).updateProgress("1", 25, 3)
      verifyNoMoreInteractions(downloadDao)
    }
  }

  @Test
  fun updateDownloadState() {
    testCoroutineRule.runBlockingTest {
      contentDataSourceLocal.updateDownloadState(
        "1",
        DownloadState.COMPLETED
      )
      verify(downloadDao).updateState("1", 3)
      verifyNoMoreInteractions(downloadDao)
    }
  }

  @Test
  fun insertDownload() {
    testCoroutineRule.runBlockingTest {
      val today = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)
      val download = Download(
        "1",
        state = 3,
        createdAt = today.format(DateTimeFormatter.ISO_DATE_TIME)
      )
      contentDataSourceLocal.insertDownload("1", DownloadState.COMPLETED, today)
      verify(downloadDao).insert(download)
      verifyNoMoreInteractions(downloadDao)
    }
  }

  @Test
  fun deleteDownload() {
    testCoroutineRule.runBlockingTest {
      contentDataSourceLocal.deleteDownload("1")
      verify(downloadDao).delete(
        Download(
          "1"
        )
      )
      verifyNoMoreInteractions(downloadDao)
    }
  }

  @Test
  fun deleteAllDownloads() {
    testCoroutineRule.runBlockingTest {
      contentDataSourceLocal.deleteAllDownloads()
      verify(downloadDao).deleteAll()
      verifyNoMoreInteractions(downloadDao)
    }
  }

  @Test
  fun getQueuedDownloads_A() {
    testCoroutineRule.runBlockingTest {
      contentDataSourceLocal.getQueuedDownloads(
        1,
        arrayOf(1, 2),
        arrayOf("screencast", "episode")
      )
      verify(downloadDao).getQueuedDownloads(1, arrayOf(1, 2), arrayOf("screencast", "episode"))
      verifyNoMoreInteractions(downloadDao)
    }
  }

  @Test
  fun getQueuedDownload() {
    testCoroutineRule.runBlockingTest {
      contentDataSourceLocal.getQueuedDownload("1")
      verify(downloadDao).getQueuedDownload("1")
      verifyNoMoreInteractions(downloadDao)
    }
  }

  @Test
  fun getQueuedDownloads_B() {
    testCoroutineRule.runBlockingTest {
      contentDataSourceLocal.getQueuedDownloads()
      verify(downloadDao).getQueuedDownloads(arrayOf("collection", "screencast"))
      verifyNoMoreInteractions(downloadDao)
    }
  }

  @Test
  fun getDownloadsById() {
    testCoroutineRule.runBlockingTest {
      val downloadIds = listOf("1", "2", "3")
      contentDataSourceLocal.getDownloadsById(downloadIds)
      verify(downloadDao).getDownloadsById(downloadIds)
      verifyNoMoreInteractions(downloadDao)
    }
  }

  @Test
  fun insertContent() {
    testCoroutineRule.runBlockingTest {
      val content = com.raywenderlich.emitron.data.createContent()
      contentDataSourceLocal.insertContent(content)
      verify(contentDao).insertOrUpdateContent(
        listOf(
          com.raywenderlich.emitron.model.entity.Content(
            contentId = "1",
            name = "Introduction to Kotlin Lambdas: Getting Started",
            description = "In this tutorial you will learn how to use lambda.",
            contributors = "Luke",
            professional = true,
            deleted = false,
            contentType = "screencast",
            difficulty = "beginner",
            releasedAt = "2019-08-08T00:00:00.000Z",
            technology = "Swift, iOS",
            duration = 408,
            streamUrl = "",
            cardArtworkUrl = "https://koenig-media.raywenderlich.com/",
            videoId = null,
            bookmarkId = "1",
            progressionId = null,
            updatedAt = ""
          )
        ),
        listOf(
          Progression(progressionId = "1", percentComplete = 99, finished = true)
        ),
        progressionDao,
        listOf(ContentDomainJoin("1", "2")),
        contentDomainJoinDao,
        listOf(
          Group(
            "1",
            "The basics",
            1
          )
        ),
        groupDao,
        listOf(ContentGroupJoin("1", "1")),
        contentGroupJoinDao,
        listOf(GroupEpisodeJoin("1", "1")),
        groupEpisodeJoinDao
      )
      verifyNoMoreInteractions(contentDao)
    }
  }

  @Test
  fun getContent() {
    testCoroutineRule.runBlockingTest {
      val expected = com.raywenderlich.emitron.data.createContentDetail()
      whenever(contentDao.getContentDetail("1")).doReturn(expected)
      val result = contentDataSourceLocal.getContent("1")
      result isEqualTo expected
      verify(contentDao).getContentDetail("1")
      verifyNoMoreInteractions(contentDao)
    }
  }
}
