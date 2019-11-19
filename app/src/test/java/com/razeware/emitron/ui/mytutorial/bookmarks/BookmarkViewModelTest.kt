package com.razeware.emitron.ui.mytutorial.bookmarks

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.*
import com.razeware.emitron.data.bookmarks.BookmarkRepository
import com.razeware.emitron.data.createContentData
import com.razeware.emitron.data.removeBookmark
import com.razeware.emitron.model.Content
import com.razeware.emitron.model.Data
import com.razeware.emitron.ui.content.ContentPagedViewModel
import com.razeware.emitron.utils.*
import org.junit.Rule
import org.junit.Test

class BookmarkViewModelTest {

  private val bookmarkRepository: BookmarkRepository = mock()

  private val contentViewModel: ContentPagedViewModel = ContentPagedViewModel()

  private val bookmarkActionDelegate: BookmarkActionDelegate = mock()

  private val boundaryCallbackNotifier: BoundaryCallbackNotifier = BoundaryCallbackNotifier()

  private lateinit var viewModel: BookmarkViewModel

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  private fun createViewModel() {
    viewModel = BookmarkViewModel(
      bookmarkRepository,
      contentViewModel,
      bookmarkActionDelegate,
      boundaryCallbackNotifier
    )
  }

  @Test
  fun init() {
    whenever(
      bookmarkActionDelegate.bookmarkActionResult
    ).doReturn(
      MutableLiveData<Event<BookmarkActionDelegate.BookmarkActionResult>>().apply {
        value = Event(BookmarkActionDelegate.BookmarkActionResult.BookmarkDeleted)
      }
    )
    createViewModel()
    viewModel.bookmarkDeleteActionResult.observeForTestingResultNullable()

    verify(bookmarkActionDelegate).bookmarkActionResult
    viewModel.bookmarkDeleteActionResult.value?.peekContent() isEqualTo
        BookmarkActionDelegate.BookmarkActionResult.BookmarkDeleted
  }

  @Test
  fun loadBookmarks() {
    createViewModel()

    val response: LocalPagedResponse<Data> = mock()
    whenever(bookmarkRepository.getBookmarks(boundaryCallbackNotifier)).doReturn(response)

    // When
    viewModel.loadBookmarks()

    // Then
    verify(bookmarkRepository).getBookmarks(boundaryCallbackNotifier)
    verifyNoMoreInteractions(bookmarkRepository)
  }

  @Test
  fun getPaginationViewModel() {
    createViewModel()
    viewModel.getPaginationViewModel() isEqualTo contentViewModel
  }

  /**
   * Test content bookmark deletion success
   */
  @Test
  fun updateContentBookmark_deleteBookmarkSuccess() {
    whenever(
      bookmarkActionDelegate.bookmarkActionResult
    ).doReturn(
      MutableLiveData<Event<BookmarkActionDelegate.BookmarkActionResult>>().apply {
        value = Event(BookmarkActionDelegate.BookmarkActionResult.BookmarkDeleted)
      }
    )
    createViewModel()
    testCoroutineRule.runBlockingTest {

      // Given
      val contentData = createContentData(
        type = "screencast",
        groups = null,
        bookmark = Content(
          datum = Data(
            id = "10",
            type = "bookmarks"
          )
        )
      )

      val expectedContentData = removeBookmark(contentData)
      whenever(
        bookmarkActionDelegate.updateContentBookmark(
          contentData, boundaryCallbackNotifier
        )
      ).doReturn(
        expectedContentData
      )

      // When
      viewModel.bookmarkDeleteActionResult.observeForTestingResultNullable()
      viewModel.updateContentBookmark(contentData)

      // Then
      verify(bookmarkActionDelegate).bookmarkActionResult
      verify(bookmarkActionDelegate).updateContentBookmark(contentData, boundaryCallbackNotifier)
      verifyNoMoreInteractions(bookmarkActionDelegate)
      viewModel.bookmarkDeleteActionResult.value?.peekContent() isEqualTo
          BookmarkActionDelegate.BookmarkActionResult.BookmarkDeleted
      boundaryCallbackNotifier.hasRequests() isEqualTo false
    }
  }

  /**
   * Test content bookmark deletion failure
   */
  @Test
  fun updateContentBookmark_deleteBookmarkFailure() {
    whenever(
      bookmarkActionDelegate.bookmarkActionResult
    ).doReturn(
      MutableLiveData<Event<BookmarkActionDelegate.BookmarkActionResult>>().apply {
        value = Event(BookmarkActionDelegate.BookmarkActionResult.BookmarkFailedToDelete)
      }
    )
    createViewModel()
    testCoroutineRule.runBlockingTest {

      // Given
      val contentData = createContentData(
        type = "screencast",
        groups = null,
        bookmark = Content(
          datum = Data(
            id = "10",
            type = "bookmarks"
          )
        )
      )

      whenever(
        bookmarkActionDelegate.updateContentBookmark(
          contentData, boundaryCallbackNotifier
        )
      ).doReturn(
        contentData
      )


      // When
      viewModel.bookmarkDeleteActionResult.observeForTestingResultNullable()
      viewModel.updateContentBookmark(contentData)

      // Then
      verify(bookmarkActionDelegate).bookmarkActionResult
      verify(bookmarkActionDelegate).updateContentBookmark(contentData, boundaryCallbackNotifier)
      verifyNoMoreInteractions(bookmarkActionDelegate)

      viewModel.bookmarkDeleteActionResult.value?.peekContent() isEqualTo
          BookmarkActionDelegate.BookmarkActionResult.BookmarkFailedToDelete
      verify(bookmarkActionDelegate).updateContentBookmark(contentData, boundaryCallbackNotifier)
      boundaryCallbackNotifier.hasRequests() isEqualTo false
    }
  }
}
