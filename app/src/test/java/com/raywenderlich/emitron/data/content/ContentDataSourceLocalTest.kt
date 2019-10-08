package com.raywenderlich.emitron.data.content

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.*
import com.raywenderlich.emitron.data.content.dao.ContentDao
import com.raywenderlich.emitron.data.content.dao.ContentDomainJoinDao
import com.raywenderlich.emitron.data.progressions.dao.ProgressionDao
import com.raywenderlich.emitron.model.*
import com.raywenderlich.emitron.model.entity.ContentDomainJoin
import com.raywenderlich.emitron.model.entity.Progression
import com.raywenderlich.emitron.utils.TestCoroutineRule
import com.raywenderlich.emitron.utils.isEqualTo
import com.raywenderlich.emitron.utils.observeForTestingResultNullable
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ContentDataSourceLocalTest {

  private val contentDao: ContentDao = mock()
  private val contentDomainJoinDao: ContentDomainJoinDao = mock()
  private val progressionDao: ProgressionDao = mock()

  private lateinit var contentDataSourceLocal: ContentDataSourceLocal

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  @Before
  fun setUp() {
    contentDataSourceLocal =
      ContentDataSourceLocal(contentDao, contentDomainJoinDao, progressionDao)
  }

  @Test
  fun insertContent() {
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
            videoId = "",
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
            videoId = "",
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
            videoId = "",
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
            videoId = "",
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
      contentDao.updateBookmark("1", "2")

      verify(contentDao).updateBookmark("1", bookmarkId = "2")
      verifyNoMoreInteractions(contentDao)
    }
  }

  @Test
  fun getProgressions() {
    contentDao.getProgressions(false, arrayOf())
    verify(contentDao).getProgressions(false, arrayOf())
    verifyNoMoreInteractions(contentDomainJoinDao)
  }

  @Test
  fun updateProgress() {
    testCoroutineRule.runBlockingTest {
      progressionDao.updateProgress("1", true)

      verify(progressionDao).updateProgress("1", true)
      verifyNoMoreInteractions(progressionDao)
    }
  }
}
