package com.raywenderlich.emitron.ui.collection

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import com.raywenderlich.emitron.data.content.ContentRepository
import com.raywenderlich.emitron.model.*
import com.raywenderlich.emitron.ui.mytutorial.bookmarks.BookmarkActionDelegate
import com.raywenderlich.emitron.ui.mytutorial.progressions.ProgressionActionDelegate
import com.raywenderlich.emitron.utils.*
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class CollectionViewModelTest {

  private val contentRepository: ContentRepository = mock()

  private val bookmarkActionDelegate: BookmarkActionDelegate = mock()

  private val progressionActionDelegate: ProgressionActionDelegate = mock()

  private lateinit var viewModel: CollectionViewModel

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  private fun createViewModel() {
    viewModel =
      CollectionViewModel(contentRepository, bookmarkActionDelegate, progressionActionDelegate)
  }

  /**
   * Test loading a collection with empty id
   */
  @Test
  fun loadCollection_emptyId() {

    createViewModel()
    val data = Data(attributes = Attributes(contentType = "screencast"))

    viewModel.collection.observeForTestingResultNullable()
    viewModel.collectionContentType.observeForTestingResultNullable()
    val uiStateObserver = viewModel.uiState.observeForTestingObserver()

    viewModel.loadCollection(data)

    assertThat(viewModel.collection.value).isEqualTo(data)
    assertThat(viewModel.collectionContentType.value).isEqualTo(ContentType.Screencast)
    assertThat(viewModel.uiState.value).isEqualTo(UiStateManager.UiState.ERROR)

    verify(uiStateObserver).onChanged(UiStateManager.UiState.LOADING)
    verify(uiStateObserver).onChanged(UiStateManager.UiState.ERROR)
    verifyNoMoreInteractions(uiStateObserver)
  }

  /**
   * Test loading a collection with episodes
   */
  @Test
  fun loadCollection() {
    createViewModel()

    testCoroutineRule.runBlockingTest {
      val contentData = createContentData()
      val content = createContent(data = contentData, included = getIncludedDataForCollection())
      val expectedEpisodes =
        listOf(
          EpisodeItem(title = "one"),
          EpisodeItem(
            data = Data(
              id = "5",
              type = "contents",
              attributes = Attributes(name = "five"),
              relationships = Relationships(
                progression = Content(
                  datum = Data(
                    id = "9",
                    type = "progressions",
                    attributes = Attributes(percentComplete = 10.0)
                  )
                )
              )
            )
          ),
          EpisodeItem(
            data = Data(
              id = "6", type = "contents",
              attributes = Attributes(name = "six"),
              relationships = Relationships()
            )
          ),
          EpisodeItem(title = "two"),
          EpisodeItem(
            data = Data(
              id = "7", type = "contents",
              attributes = Attributes(name = "seven"),
              relationships = Relationships()
            )
          ),
          EpisodeItem(
            data = Data(
              id = "8", type = "contents",
              attributes = Attributes(name = "eight"),
              relationships = Relationships()
            )
          )
        )
      whenever(contentRepository.getContent("1")).doReturn(content)

      val data = Data(id = "1", attributes = Attributes(contentType = "collection"))

      viewModel.collection.observeForTestingResultNullable()
      viewModel.collectionEpisodes.observeForTestingResultNullable()
      val uiStateObserver = viewModel.uiState.observeForTestingObserver()

      viewModel.loadCollection(data)

      assertThat(viewModel.collection.value).isEqualTo(contentData)
      assertThat(viewModel.collectionEpisodes.value).isEqualTo(expectedEpisodes)


      verify(uiStateObserver).onChanged(UiStateManager.UiState.LOADING)
      verify(uiStateObserver).onChanged(UiStateManager.UiState.LOADED)
      verifyNoMoreInteractions(uiStateObserver)
    }
  }

  /**
   * Test loading a collection without episodes
   */
  @Test
  fun loadCollection_typeScreencast() {
    createViewModel()

    testCoroutineRule.runBlockingTest {
      val contentData = createContentData(type = "screencast")
      val content = createContent(data = contentData)
      whenever(contentRepository.getContent("1")).doReturn(content)

      val data = Data(id = "1", attributes = Attributes(contentType = "screencast"))

      viewModel.collection.observeForTestingResultNullable()
      viewModel.collectionEpisodes.observeForTestingResultNullable()
      val uiStateObserver = viewModel.uiState.observeForTestingObserver()
      val collectionEpisodeObserver = viewModel.collectionEpisodes.observeForTestingObserver()

      viewModel.loadCollection(data)

      assertThat(viewModel.collection.value).isEqualTo(contentData)
      assertThat(viewModel.collectionEpisodes.value).isEqualTo(null)


      verifyZeroInteractions(collectionEpisodeObserver)
      verify(uiStateObserver).onChanged(UiStateManager.UiState.LOADING)
      verify(uiStateObserver).onChanged(UiStateManager.UiState.LOADED)
      verifyNoMoreInteractions(uiStateObserver)
    }
  }

  /**
   * Test collection load api error
   */
  @Test
  fun loadCollection_apiError() {
    createViewModel()
    testCoroutineRule.runBlockingTest {
      // Given
      whenever(contentRepository.getContent("1")).doThrow(IOException())

      val data = Data(id = "1", attributes = Attributes(contentType = "screencast"))

      viewModel.collection.observeForTestingResultNullable()
      viewModel.collectionEpisodes.observeForTestingResultNullable()
      val uiStateObserver = viewModel.uiState.observeForTestingObserver()
      val collectionEpisodeObserver = viewModel.collectionEpisodes.observeForTestingObserver()
      val collectionObserver = viewModel.collection.observeForTestingObserver()
      // When
      viewModel.loadCollection(data)

      // Then
      assertThat(viewModel.collection.value).isEqualTo(data)
      assertThat(viewModel.collectionEpisodes.value).isEqualTo(null)
      verify(collectionObserver).onChanged(data)
      verifyNoMoreInteractions(collectionObserver)
      verifyZeroInteractions(collectionEpisodeObserver)
      verify(uiStateObserver).onChanged(UiStateManager.UiState.LOADING)
      verify(uiStateObserver).onChanged(UiStateManager.UiState.LOADED)
      verifyNoMoreInteractions(uiStateObserver)
    }
  }

  /**
   * Test getting playlist when content type is collection
   */
  @Test
  fun getPlaylist() {
    createViewModel()
    testCoroutineRule.runBlockingTest {
      // Given
      val expectedPlaylist =
        listOf(
          Data(
            id = "5",
            type = "contents",
            attributes = Attributes(name = "five"),
            relationships = Relationships(
              progression = Content(
                datum = Data(
                  id = "9",
                  type = "progressions",
                  attributes = Attributes(percentComplete = 10.0)
                )
              )
            )
          ),
          Data(
            id = "6", type = "contents",
            attributes = Attributes(name = "six"),
            relationships = Relationships()
          ),
          Data(
            id = "7", type = "contents",
            attributes = Attributes(name = "seven"),
            relationships = Relationships()
          )
          ,
          Data(
            id = "8", type = "contents",
            attributes = Attributes(name = "eight"),
            relationships = Relationships()
          )
        )

      val contentData = createContentData()
      val content = createContent(data = contentData, included = getIncludedDataForCollection())
      whenever(contentRepository.getContent("1")).doReturn(content)

      viewModel.loadCollection(Data(id = "1"))

      // When
      val result = viewModel.getPlaylist()

      // Then
      assertThat(result).isEqualTo(Contents(datum = expectedPlaylist))
    }
  }

  /**
   * Test getting playlist when content type is screencast
   */
  @Test
  fun getPlaylist_typeScreencast() {
    createViewModel()
    testCoroutineRule.runBlockingTest {

      // Given
      val contentData = createContentData(type = "screencast")
      val content = createContent(data = contentData)
      whenever(contentRepository.getContent("1")).doReturn(content)

      viewModel.loadCollection(Data(id = "1"))

      // When
      val result = viewModel.getPlaylist()

      // Then
      assertThat(result).isEqualTo(Contents(datum = listOf(contentData)))
    }
  }

  /**
   * Test content bookmarking success
   */
  @Test
  fun updateContentBookmark_createBookmarkSuccess() {
    whenever(
      bookmarkActionDelegate.bookmarkActionResult
    ).doReturn(
      MutableLiveData<Event<BookmarkActionDelegate.BookmarkActionResult>>().apply {
        value = Event(BookmarkActionDelegate.BookmarkActionResult.BookmarkCreated)
      }
    )
    createViewModel()
    testCoroutineRule.runBlockingTest {

      // Given
      whenever(contentRepository.getContent("1")).doReturn(createContent())
      viewModel.loadCollection(Data(id = "1"))


      val expectedContent = viewModel.collection.value?.addBookmark(createBookmarkResponse())
      whenever(bookmarkActionDelegate.updateContentBookmark(createContentData(), null)).doReturn(
        expectedContent
      )

      // When
      viewModel.bookmarkActionResult.observeForTestingResultNullable()
      viewModel.collection.observeForTestingResultNullable()
      viewModel.updateContentBookmark()

      with(viewModel) {
        verify(bookmarkActionDelegate).bookmarkActionResult
        verify(bookmarkActionDelegate).updateContentBookmark(createContentData())
        verifyNoMoreInteractions(bookmarkActionDelegate)
        collection.value isEqualTo expectedContent
        collection.value?.isBookmarked() isEqualTo true
        bookmarkActionResult.value?.peekContent() isEqualTo
            BookmarkActionDelegate.BookmarkActionResult.BookmarkCreated
      }
    }
  }

  /**
   * Test content bookmarking failure
   */
  @Test
  fun updateContentBookmark_createBookmarkFailure() {
    whenever(
      bookmarkActionDelegate.bookmarkActionResult
    ).doReturn(
      MutableLiveData<Event<BookmarkActionDelegate.BookmarkActionResult>>().apply {
        value = Event(BookmarkActionDelegate.BookmarkActionResult.BookmarkFailedToCreate)
      }
    )
    createViewModel()
    testCoroutineRule.runBlockingTest {

      // Given
      whenever(contentRepository.getContent("1")).doReturn(createContent())
      viewModel.loadCollection(Data(id = "1"))


      val expectedContent = viewModel.collection.value
      whenever(
        bookmarkActionDelegate.updateContentBookmark(
          createContentData(), null
        )
      ).doReturn(
        expectedContent
      )

      // When
      viewModel.bookmarkActionResult.observeForTestingResultNullable()
      viewModel.collection.observeForTestingResultNullable()
      viewModel.updateContentBookmark()

      with(viewModel) {
        verify(bookmarkActionDelegate).bookmarkActionResult
        verify(bookmarkActionDelegate).updateContentBookmark(createContentData())
        verifyNoMoreInteractions(bookmarkActionDelegate)
        collection.value isEqualTo expectedContent
        collection.value?.isBookmarked() isEqualTo false
        bookmarkActionResult.value?.peekContent() isEqualTo
            BookmarkActionDelegate.BookmarkActionResult.BookmarkFailedToCreate
      }
    }
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
      val content = createContent(data = contentData)
      whenever(contentRepository.getContent("1")).doReturn(content)
      viewModel.loadCollection(Data(id = "1"))

      val expectedContentData = removeBookmark(contentData)
      whenever(
        bookmarkActionDelegate.updateContentBookmark(
          contentData, null
        )
      ).doReturn(
        expectedContentData
      )

      // When
      viewModel.bookmarkActionResult.observeForTestingResultNullable()
      viewModel.collection.observeForTestingResultNullable()
      viewModel.updateContentBookmark()

      // Then
      with(viewModel) {
        verify(bookmarkActionDelegate).bookmarkActionResult
        verify(bookmarkActionDelegate).updateContentBookmark(contentData)
        verifyNoMoreInteractions(bookmarkActionDelegate)
        collection.value isEqualTo expectedContentData
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
      val content = createContent(data = contentData)
      whenever(contentRepository.getContent("1")).doReturn(content)

      viewModel.loadCollection(Data(id = "1"))

      whenever(
        bookmarkActionDelegate.updateContentBookmark(
          contentData, null
        )
      ).doReturn(
        contentData
      )


      // When
      viewModel.bookmarkActionResult.observeForTestingResultNullable()
      viewModel.collection.observeForTestingResultNullable()
      viewModel.updateContentBookmark()

      // Then
      with(viewModel) {
        verify(bookmarkActionDelegate).bookmarkActionResult
        verify(bookmarkActionDelegate).updateContentBookmark(contentData)
        verifyNoMoreInteractions(bookmarkActionDelegate)
        collection.value isEqualTo contentData
        bookmarkActionResult.value?.peekContent() isEqualTo
            BookmarkActionDelegate.BookmarkActionResult.BookmarkFailedToDelete
      }
    }
  }

  @Test
  fun updateContentProgression_markCompletedSuccess() {
    whenever(
      progressionActionDelegate.completionActionResult
    ).doReturn(
      MutableLiveData<Pair<Event<ProgressionActionDelegate.EpisodeProgressionActionResult>, Int>>().apply {
        value =
          Event(ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeMarkedCompleted) to 4
      }
    )
    createViewModel()
    testCoroutineRule.runBlockingTest {

      val episodeData = Data(
        id = "8", type = "contents",
        attributes = Attributes(name = "eight"),
        relationships = Relationships(
          progression = Content(
            datum = Data(
              id = "10",
              attributes = Attributes(finished = false),
              type = "progression"
            )
          )
        )
      )

      val episodePosition = 4

      // When
      viewModel.completionActionResult.observeForTestingResultNullable()
      viewModel.updateContentProgression(episodeData, episodePosition)

      // Then
      verify(progressionActionDelegate).completionActionResult
      verify(progressionActionDelegate).updateContentProgression(episodeData, episodePosition)
      verifyNoMoreInteractions(progressionActionDelegate)
      viewModel.completionActionResult.value?.first?.peekContent() isEqualTo
          ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeMarkedCompleted
      viewModel.completionActionResult.value?.second isEqualTo
          episodePosition
    }
  }

  @Test
  fun updateContentProgression_markCompletedFailure() {
    whenever(
      progressionActionDelegate.completionActionResult
    ).doReturn(
      MutableLiveData<Pair<Event<ProgressionActionDelegate.EpisodeProgressionActionResult>, Int>>().apply {
        value =
          Event(ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeFailedToMarkComplete) to 4
      }
    )
    createViewModel()
    testCoroutineRule.runBlockingTest {

      // Given
      val episodeData = Data(
        id = "8", type = "contents",
        attributes = Attributes(name = "eight"),
        relationships = Relationships(
          progression = Content(
            datum = Data(
              id = "10",
              attributes = Attributes(finished = false),
              type = "progression"
            )
          )
        )
      )

      val episodePosition = 4

      // When
      viewModel.completionActionResult.observeForTestingResultNullable()
      viewModel.updateContentProgression(episodeData, episodePosition)

      // Then
      verify(progressionActionDelegate).completionActionResult
      verify(progressionActionDelegate).updateContentProgression(episodeData, episodePosition)
      verifyNoMoreInteractions(progressionActionDelegate)
      viewModel.completionActionResult.value?.first?.peekContent() isEqualTo
          ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeFailedToMarkComplete
      viewModel.completionActionResult.value?.second isEqualTo
          episodePosition
    }
  }

  @Test
  fun toggleEpisodeCompleted_markInProgressSuccess() {
    whenever(
      progressionActionDelegate.completionActionResult
    ).doReturn(
      MutableLiveData<Pair<Event<ProgressionActionDelegate.EpisodeProgressionActionResult>, Int>>().apply {
        value =
          Event(ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeMarkedInProgress) to 4
      }
    )
    createViewModel()
    testCoroutineRule.runBlockingTest {

      // Given
      val episodeData = Data(
        id = "8", type = "contents",
        attributes = Attributes(name = "eight"),
        relationships = Relationships(
          progression = Content(
            datum = Data(
              id = "10",
              attributes = Attributes(finished = true),
              type = "progression"
            )
          )
        )
      )

      val episodePosition = 4

      // When
      viewModel.completionActionResult.observeForTestingResultNullable()
      viewModel.updateContentProgression(episodeData, episodePosition)

      // Then
      verify(progressionActionDelegate).completionActionResult
      verify(progressionActionDelegate).updateContentProgression(episodeData, episodePosition)
      verifyNoMoreInteractions(progressionActionDelegate)
      viewModel.completionActionResult.value?.first?.peekContent() isEqualTo
          ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeMarkedInProgress
      viewModel.completionActionResult.value?.second isEqualTo
          episodePosition
    }
  }

  @Test
  fun updateContentProgression_markInProgressFailure() {
    whenever(
      progressionActionDelegate.completionActionResult
    ).doReturn(
      MutableLiveData<Pair<Event<ProgressionActionDelegate.EpisodeProgressionActionResult>, Int>>().apply {
        value =
          Event(ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeFailedToMarkInProgress) to 4
      }
    )
    createViewModel()
    testCoroutineRule.runBlockingTest {

      // Given
      val episodeData = Data(
        id = "8", type = "contents",
        attributes = Attributes(name = "eight"),
        relationships = Relationships(
          progression = Content(
            datum = Data(
              id = "10",
              attributes = Attributes(finished = true),
              type = "progression"
            )
          )
        )
      )

      val episodePosition = 4

      // When
      viewModel.completionActionResult.observeForTestingResultNullable()
      viewModel.updateContentProgression(episodeData, episodePosition)

      // Then
      verify(progressionActionDelegate).completionActionResult
      verify(progressionActionDelegate).updateContentProgression(episodeData, episodePosition)
      verifyNoMoreInteractions(progressionActionDelegate)
      viewModel.completionActionResult.value?.first?.peekContent() isEqualTo
          ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeFailedToMarkInProgress
      viewModel.completionActionResult.value?.second isEqualTo
          episodePosition
    }
  }
}
