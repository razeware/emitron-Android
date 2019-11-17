package com.razeware.emitron.ui.mytutorial.bookmarks

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.*
import com.razeware.emitron.data.bookmarks.BookmarkRepository
import com.razeware.emitron.data.createContent
import com.razeware.emitron.data.createContentData
import com.razeware.emitron.data.removeBookmark
import com.razeware.emitron.model.Content
import com.razeware.emitron.model.Data
import com.razeware.emitron.utils.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class BookmarkActionDelegateTest {


  private lateinit var viewModel: BookmarkActionDelegate

  private val bookmarkRepository: BookmarkRepository = mock()

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  @Before
  fun setUp() {
    viewModel = BookmarkActionDelegate(bookmarkRepository)
  }

  /**
   * Test content bookmarking success
   */
  @Test
  fun updateContentBookmark_createBookmarkSuccess() {

    testCoroutineRule.runBlockingTest {

      // Given
      val contentData = createContentData(
        id = "10",
        type = "screencast",
        groups = null
      )
      val bookmark = Content(
        datum = Data(
          id = "10",
          type = "bookmarks"
        )
      )
      val response = createContent(
        data = createContentData(
          id = "10",
          bookmark = bookmark
        )
      )
      whenever(bookmarkRepository.createBookmark("10")).doReturn(response to true)
      val boundaryCallbackNotifier = BoundaryCallbackNotifier()

      // When
      viewModel.bookmarkActionResult.observeForTestingResultNullable()
      val result = viewModel.updateContentBookmark(contentData, boundaryCallbackNotifier)

      // Then
      val expectedContent = contentData.addBookmark(response)

      with(viewModel) {
        result isEqualTo expectedContent
        result?.isBookmarked() isEqualTo true
        boundaryCallbackNotifier.hasRequests() isEqualTo false
        bookmarkActionResult.value?.peekContent() isEqualTo
            BookmarkActionDelegate.BookmarkActionResult.BookmarkCreated
        verify(bookmarkRepository).createBookmark("10")
        verify(bookmarkRepository).updateBookmarkInDb("10", "10")
        verifyNoMoreInteractions(bookmarkRepository)
      }
    }
  }

  /**
   * Test content bookmarking failure
   */
  @Test
  fun updateContentBookmark_createBookmarkFailure() {

    testCoroutineRule.runBlockingTest {

      // Given
      val contentData = createContentData(
        id = "10",
        type = "screencast",
        groups = null
      )
      val content = createContent(data = contentData)
      whenever(bookmarkRepository.createBookmark("10")).doReturn(content to false)

      // When
      viewModel.bookmarkActionResult.observeForTestingResultNullable()
      viewModel.updateContentBookmark(contentData)

      // Then
      verify(bookmarkRepository).createBookmark("10")
      verify(bookmarkRepository).updateBookmarkInDb("10", null)
      verifyNoMoreInteractions(bookmarkRepository)
      viewModel.bookmarkActionResult.value?.peekContent() isEqualTo
          BookmarkActionDelegate.BookmarkActionResult.BookmarkFailedToCreate
    }
  }

  /**
   * Test content bookmarking API error
   */
  @Test
  fun updateContentBookmark_createBookmarkApiError() {
    testCoroutineRule.runBlockingTest {

      // Given
      val contentData = createContentData(
        id = "10",
        type = "screencast",
        groups = null
      )
      whenever(bookmarkRepository.createBookmark("10")).doThrow(IOException())

      // When
      viewModel.bookmarkActionResult.observeForTestingResultNullable()
      viewModel.updateContentBookmark(contentData)

      // Then
      verify(bookmarkRepository).createBookmark("10")
      verify(bookmarkRepository).updateBookmarkInDb("10", null)
      verifyNoMoreInteractions(bookmarkRepository)
      viewModel.bookmarkActionResult.value?.peekContent() isEqualTo
          BookmarkActionDelegate.BookmarkActionResult.BookmarkFailedToCreate
    }
  }

  /**
   * Test content bookmark deletion success
   */
  @Test
  fun updateContentBookmark_deleteBookmarkSuccess() {
    testCoroutineRule.runBlockingTest {

      // Given
      val contentData = createContentData(
        id = "10",
        type = "screencast",
        groups = null,
        bookmark = Content(
          datum = Data(
            id = "10",
            type = "bookmarks"
          )
        )
      )
      whenever(bookmarkRepository.deleteBookmark("10", "10")).doReturn(true)


      // When
      viewModel.bookmarkActionResult.observeForTestingResultNullable()
      val result = viewModel.updateContentBookmark(contentData)

      // Then
      val expectedContentData = removeBookmark(contentData)

      verify(bookmarkRepository).deleteBookmark("10", "10")
      verify(bookmarkRepository).updateBookmarkInDb("10", null)
      verifyNoMoreInteractions(bookmarkRepository)

      with(viewModel) {
        result isEqualTo expectedContentData
        bookmarkActionResult.value?.peekContent() isEqualTo
            BookmarkActionDelegate.BookmarkActionResult.BookmarkDeleted
      }
    }
  }

  /**
   * Test content bookmark deletion failure
   */
  @Test
  fun updateContentBookmark_deleteBookmarkFailure() {
    testCoroutineRule.runBlockingTest {

      // Given
      val contentData = createContentData(
        id = "10",
        type = "screencast",
        groups = null,
        bookmark = Content(
          datum = Data(
            id = "10",
            type = "bookmarks"
          )
        )
      )
      whenever(bookmarkRepository.deleteBookmark("10", "10")).doReturn(false)

      // When
      viewModel.bookmarkActionResult.observeForTestingResultNullable()
      viewModel.updateContentBookmark(contentData)

      // Then
      verify(bookmarkRepository).deleteBookmark("10", "10")
      verify(bookmarkRepository).updateBookmarkInDb("10", "10")
      verifyNoMoreInteractions(bookmarkRepository)

      viewModel.bookmarkActionResult.value?.peekContent() isEqualTo
          BookmarkActionDelegate.BookmarkActionResult.BookmarkFailedToDelete
    }
  }

  /**
   * Test content bookmark deletion api error
   */
  @Test
  fun updateContentBookmark_deleteBookmarkApiError() {
    testCoroutineRule.runBlockingTest {

      // Given
      val contentData = createContentData(
        id = "10",
        type = "screencast",
        bookmark = Content(datum = Data(id = "10"))
      )
      whenever(bookmarkRepository.deleteBookmark("10", "10")).doThrow(IOException())

      // When
      viewModel.bookmarkActionResult.observeForTestingResultNullable()
      viewModel.updateContentBookmark(contentData)

      // Then
      verify(bookmarkRepository).deleteBookmark("10", "10")
      verify(bookmarkRepository).updateBookmarkInDb("10", "10")
      verifyNoMoreInteractions(bookmarkRepository)
      viewModel.bookmarkActionResult.value?.peekContent() isEqualTo
          BookmarkActionDelegate.BookmarkActionResult.BookmarkFailedToDelete
    }
  }
}
