package com.razeware.emitron.data.content

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.*
import com.razeware.emitron.data.content.dao.*
import com.razeware.emitron.data.filter.dao.CategoryDao
import com.razeware.emitron.data.filter.dao.DomainDao
import com.razeware.emitron.data.progressions.dao.ProgressionDao
import com.razeware.emitron.model.*
import com.razeware.emitron.model.Content
import com.razeware.emitron.model.entity.*
import com.razeware.emitron.utils.TestCoroutineRule
import com.razeware.emitron.utils.isEqualTo
import com.razeware.emitron.utils.observeForTestingResultNullable
import org.junit.Before
import org.junit.Rule
import org.junit.Test

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
            cardArtworkUrl = "https://koenig-media.razeware.com/",
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
            url = "https://koenig-media.razeware.com/",
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
            cardArtworkUrl = "https://koenig-media.razeware.com/",
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
            url = "https://koenig-media.razeware.com/",
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
          com.razeware.emitron.model.entity.Content(
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
            streamUrl = "",
            cardArtworkUrl = "https://koenig-media.razeware.com/",
            videoId = null,
            bookmarkId = "1",
            updatedAt = ""
          ),
          com.razeware.emitron.model.entity.Content(
            contentId = "2",
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
            streamUrl = "",
            cardArtworkUrl = "https://koenig-media.razeware.com/",
            videoId = null,
            bookmarkId = "2",
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
            cardArtworkUrl = "https://koenig-media.razeware.com/",
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
            url = "https://koenig-media.razeware.com/",
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
                relationships = Relationships(
                  content = Content(
                    datum = Data(id = "1")
                  )
                ),
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
            cardArtworkUrl = "https://koenig-media.razeware.com/",
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
            url = "https://koenig-media.razeware.com/",
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
                relationships = Relationships(
                  content = Content(
                    datum = Data(id = "2")
                  )
                ),
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
          com.razeware.emitron.model.entity.Content(
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
            streamUrl = "",
            cardArtworkUrl = "https://koenig-media.razeware.com/",
            videoId = null,
            bookmarkId = "1",
            updatedAt = ""
          ),
          com.razeware.emitron.model.entity.Content(
            contentId = "2",
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
            streamUrl = "",
            cardArtworkUrl = "https://koenig-media.razeware.com/",
            videoId = null,
            bookmarkId = "2",
            updatedAt = ""
          )
        ),
        listOf(
          Progression(
            contentId = "1",
            progressionId = "1",
            percentComplete = 99,
            finished = true,
            synced = true,
            updatedAt = ""
          ),
          Progression(
            contentId = "2",
            progressionId = "2",
            percentComplete = 50,
            finished = false,
            synced = true,
            updatedAt = ""
          )
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
    val content = com.razeware.emitron.model.entity.Content(
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
      streamUrl = "https://koenig-media.razeware.com/",
      cardArtworkUrl = "https://koenig-media.razeware.com/",
      videoId = "1",
      bookmarkId = "1",
      updatedAt = "2019-08-08T00:00:00.000Z"
    )
    val contents = MutableLiveData<List<com.razeware.emitron.model.entity.Content>>().apply {
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
  fun insertContent() {
    testCoroutineRule.runBlockingTest {
      val content = com.razeware.emitron.data.createContent()
      contentDataSourceLocal.insertContent(content)
      verify(contentDao).insertOrUpdateContent(
        listOf(
          com.razeware.emitron.model.entity.Content(
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
            streamUrl = "",
            cardArtworkUrl = "https://koenig-media.razeware.com/",
            videoId = null,
            bookmarkId = "1",
            updatedAt = ""
          )
        ),
        listOf(
          Progression(
            contentId = "1",
            progressionId = "1",
            percentComplete = 99,
            finished = true,
            synced = true,
            updatedAt = ""
          )
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
      val expected = com.razeware.emitron.data.createContentDetail()
      whenever(contentDao.getContentDetail("1")).doReturn(expected)
      val result = contentDataSourceLocal.getContent("1")
      result isEqualTo expected
      verify(contentDao).getContentDetail("1")
      verifyNoMoreInteractions(contentDao)
    }
  }
}
