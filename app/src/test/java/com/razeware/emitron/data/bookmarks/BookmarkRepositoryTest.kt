package com.razeware.emitron.data.bookmarks

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.*
import com.razeware.emitron.data.content.ContentDataSourceLocal
import com.razeware.emitron.model.*
import com.razeware.emitron.utils.TestCoroutineRule
import com.razeware.emitron.utils.async.ThreadManager
import kotlinx.coroutines.Dispatchers
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import retrofit2.Response

class BookmarkRepositoryTest {

  private lateinit var repository: BookmarkRepository

  private val bookmarkApi: BookmarkApi = mock()

  private val contentDataSourceLocal: ContentDataSourceLocal = mock()

  private val threadManager: ThreadManager = mock()

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  @Before
  fun setUp() {
    whenever(threadManager.io).doReturn(Dispatchers.Unconfined)
    repository = BookmarkRepository(bookmarkApi, threadManager, contentDataSourceLocal)
  }

  @Test
  fun createBookmark() {
    testCoroutineRule.runBlockingTest {
      val expectedContent = Content()
      val progression = createBookmarkStub()
      whenever(bookmarkApi.createBookmark(any())).doReturn(
        Response.success(
          200,
          expectedContent
        )
      )
      val (result, isSuccessful) = repository.createBookmark("1")
      verify(bookmarkApi).createBookmark(progression)
      Truth.assertThat(result).isEqualTo(expectedContent)
      Truth.assertThat(isSuccessful).isTrue()
      verifyNoMoreInteractions(bookmarkApi)
    }
  }

  @Test
  fun createBookmark_failure() {
    testCoroutineRule.runBlockingTest {
      val expectedContent = createBookmarkStub()
      val responseBody: ResponseBody = mock()
      val errorResponse: Response<Content> = Response.error(401, responseBody)
      whenever(bookmarkApi.createBookmark(any())).doReturn(errorResponse)

      val (_, result) = repository.createBookmark("1")

      verify(bookmarkApi).createBookmark(expectedContent)
      Truth.assertThat(result).isFalse()
      verifyNoMoreInteractions(bookmarkApi)
    }
  }

  @Test
  fun deleteBookmark() {
    testCoroutineRule.runBlockingTest {
      whenever(bookmarkApi.deleteBookmark(ArgumentMatchers.anyString()))
        .doReturn(Response.success(200, Any()))

      val result = repository.deleteBookmark("1", "2")
      verify(bookmarkApi).deleteBookmark("2")
      Truth.assertThat(result).isTrue()
      verifyNoMoreInteractions(bookmarkApi)
    }
  }

  @Test
  fun deleteBookmark_failure() {
    testCoroutineRule.runBlockingTest {
      val responseBody: ResponseBody = mock()
      val errorResponse: Response<Any> = Response.error(401, responseBody)
      whenever(bookmarkApi.deleteBookmark(ArgumentMatchers.anyString())).doReturn(errorResponse)

      val result2 = repository.deleteBookmark("1", "2")

      verify(bookmarkApi).deleteBookmark("2")
      Truth.assertThat(result2).isFalse()
      verifyNoMoreInteractions(bookmarkApi)
    }
  }

  @Test
  fun updateBookmarkInDb() {
    testCoroutineRule.runBlockingTest {
      repository.updateBookmarkInDb("1", "2")

      verify(contentDataSourceLocal).updateBookmark("1", bookmarkId = "2")
      verifyNoMoreInteractions(contentDataSourceLocal)
    }
  }

  private fun createBookmarkStub() = Content(
    datum = Data(
      type = DataType.Bookmarks.toRequestFormat(),
      relationships = Relationships(
        content =
        Content(
          datum = Data(
            type = DataType.Contents.toRequestFormat(),
            id = "1"
          )
        )
      )
    )
  )
}
