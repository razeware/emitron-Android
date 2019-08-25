package com.raywenderlich.emitron.ui.collection

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import com.raywenderlich.emitron.data.bookmarks.BookmarkRepository
import com.raywenderlich.emitron.data.content.ContentRepository
import com.raywenderlich.emitron.data.progressions.ProgressionRepository
import com.raywenderlich.emitron.model.*
import com.raywenderlich.emitron.utils.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class CollectionViewModelTest {

  private val contentRepository: ContentRepository = mock()

  private val bookmarkRepository: BookmarkRepository = mock()

  private val progressionRepository: ProgressionRepository = mock()

  private lateinit var viewModel: CollectionViewModel

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  @Before
  fun setUp() {
    viewModel = CollectionViewModel(contentRepository, bookmarkRepository, progressionRepository)
  }

  /**
   * Test loading a collection with empty id
   */
  @Test
  fun loadCollection_emptyId() {

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

    testCoroutineRule.runBlockingTest {
      // Given
      whenever(contentRepository.getContent("1")).doThrow(IOException())

      val data = Data(id = "1", attributes = Attributes(contentType = "screencast"))

      viewModel.collection.observeForTestingResultNullable()
      viewModel.collectionEpisodes.observeForTestingResultNullable()
      val uiStateObserver = viewModel.uiState.observeForTestingObserver()
      val collectionEpisodeObserver = viewModel.collectionEpisodes.observeForTestingObserver()

      // When
      viewModel.loadCollection(data)

      // Then
      assertThat(viewModel.collection.value).isEqualTo(null)
      assertThat(viewModel.collectionEpisodes.value).isEqualTo(null)
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
  fun toggleBookmark_createBookmarkSuccess() {

    testCoroutineRule.runBlockingTest {

      // Given
      val contentData = createContentData(
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
          isBookmarked = true,
          bookmark = bookmark
        )
      )
      val content = createContent(data = contentData)
      whenever(contentRepository.getContent("1")).doReturn(content)
      whenever(bookmarkRepository.createBookmark("1")).doReturn(response to true)
      viewModel.loadCollection(Data(id = "1"))

      // When
      viewModel.bookmarkActionResult.observeForTestingResultNullable()
      viewModel.toggleBookmark()

      // Then
      val expectedContent = viewModel.collection.value?.copy(
        attributes = viewModel.collection.value?.attributes?.copy(bookmarked = true),
        relationships = viewModel.collection.value?.relationships?.copy(
          bookmark = response
        )
      )
      with(viewModel) {
        collection.value isEqualTo expectedContent
        collection.value?.isBookmarked() isEqualTo true
        bookmarkActionResult.value?.peekContent() isEqualTo
            CollectionViewModel.BookmarkActionResult.BookmarkCreated
        verify(bookmarkRepository).createBookmark("1")
        verifyNoMoreInteractions(bookmarkRepository)
      }
    }
  }

  /**
   * Test content bookmarking failure
   */
  @Test
  fun toggleBookmark_createBookmarkFailure() {

    testCoroutineRule.runBlockingTest {

      // Given
      val contentData = createContentData(
        type = "screencast",
        groups = null
      )
      val content = createContent(data = contentData)
      whenever(contentRepository.getContent("1")).doReturn(content)
      whenever(bookmarkRepository.createBookmark("1")).doReturn(content to false)

      viewModel.loadCollection(Data(id = "1"))

      // When
      viewModel.bookmarkActionResult.observeForTestingResultNullable()
      viewModel.toggleBookmark()

      // Then
      verify(bookmarkRepository).createBookmark("1")
      verifyNoMoreInteractions(bookmarkRepository)
      viewModel.bookmarkActionResult.value?.peekContent() isEqualTo
          CollectionViewModel.BookmarkActionResult.BookmarkFailedToCreate
    }
  }

  /**
   * Test content bookmarking API error
   */
  @Test
  fun toggleBookmark_createBookmarkApiError() {
    testCoroutineRule.runBlockingTest {

      // Given
      val contentData = createContentData(
        type = "screencast",
        groups = null
      )
      val content = createContent(data = contentData)
      whenever(contentRepository.getContent("1")).doReturn(content)
      whenever(bookmarkRepository.createBookmark("1")).doThrow(IOException())

      viewModel.loadCollection(Data(id = "1"))

      // When
      viewModel.bookmarkActionResult.observeForTestingResultNullable()
      viewModel.toggleBookmark()

      // Then
      verify(bookmarkRepository).createBookmark("1")
      verifyNoMoreInteractions(bookmarkRepository)
      viewModel.bookmarkActionResult.value?.peekContent() isEqualTo
          CollectionViewModel.BookmarkActionResult.BookmarkFailedToCreate
    }
  }

  /**
   * Test content bookmark deletion success
   */
  @Test
  fun toggleBookmark_deleteBookmarkSuccess() {
    testCoroutineRule.runBlockingTest {

      // Given
      val contentData = createContentData(
        type = "screencast",
        isBookmarked = true,
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
      whenever(bookmarkRepository.deleteBookmark("10")).doReturn(true)

      viewModel.loadCollection(Data(id = "1"))

      // When
      viewModel.bookmarkActionResult.observeForTestingResultNullable()
      viewModel.toggleBookmark()

      // Then
      val expectedContentData = removeBookmark(contentData)

      verify(bookmarkRepository).deleteBookmark("10")
      verifyNoMoreInteractions(bookmarkRepository)

      with(viewModel) {
        collection.value isEqualTo expectedContentData
        bookmarkActionResult.value?.peekContent() isEqualTo
            CollectionViewModel.BookmarkActionResult.BookmarkDeleted
      }
    }
  }

  /**
   * Test content bookmark deletion failure
   */
  @Test
  fun toggleBookmark_deleteBookmarkFailure() {
    testCoroutineRule.runBlockingTest {

      // Given
      val contentData = createContentData(
        type = "screencast",
        isBookmarked = true,
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
      whenever(bookmarkRepository.deleteBookmark("10")).doReturn(false)

      viewModel.loadCollection(Data(id = "1"))

      // When
      viewModel.bookmarkActionResult.observeForTestingResultNullable()
      viewModel.collection.observeForTestingResultNullable()
      viewModel.toggleBookmark()

      // Then
      verify(bookmarkRepository).deleteBookmark("10")
      verifyNoMoreInteractions(bookmarkRepository)

      viewModel.bookmarkActionResult.value?.peekContent() isEqualTo
          CollectionViewModel.BookmarkActionResult.BookmarkFailedToDelete
    }
  }

  /**
   * Test content bookmark deletion api error
   */
  @Test
  fun toggleBookmark_deleteBookmarkApiError() {
    testCoroutineRule.runBlockingTest {

      // Given
      val contentData = createContentData(
        type = "screencast",
        isBookmarked = true,
        bookmark = Content(datum = Data(id = "10"))
      )
      val content = createContent(data = contentData)
      whenever(contentRepository.getContent("1")).doReturn(content)
      whenever(bookmarkRepository.deleteBookmark("10")).doThrow(IOException())

      viewModel.loadCollection(Data(id = "1"))

      // When
      viewModel.bookmarkActionResult.observeForTestingResultNullable()
      viewModel.toggleBookmark()

      // Then
      verify(bookmarkRepository).deleteBookmark("10")
      verifyNoMoreInteractions(bookmarkRepository)
      viewModel.bookmarkActionResult.value?.peekContent() isEqualTo
          CollectionViewModel.BookmarkActionResult.BookmarkFailedToDelete
    }
  }

  @Test
  fun toggleEpisodeCompleted_markCompletedSuccess() {
    testCoroutineRule.runBlockingTest {

      // Given
      val contentData = createContentData()
      val content = createContent(data = contentData)
      whenever(contentRepository.getContent("1")).doReturn(content)

      val response = createContent(
        data = createContentData(
          id = "10"
        )
      )
      whenever(progressionRepository.updateProgression("8")).doReturn(response to true)

      viewModel.loadCollection(Data(id = "1"))

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
      viewModel.toggleEpisodeCompleted(episodeData, episodePosition)

      // Then
      verify(progressionRepository).updateProgression("8")
      verifyNoMoreInteractions(progressionRepository)

      viewModel.completionActionResult.value?.first?.peekContent() isEqualTo
          CollectionViewModel.EpisodeProgressionActionResult.EpisodeMarkedCompleted
      viewModel.completionActionResult.value?.second isEqualTo
          episodePosition
    }
  }

  @Test
  fun toggleEpisodeCompleted_markCompletedFailure() {
    testCoroutineRule.runBlockingTest {

      // Given
      val contentData = createContentData()
      val content = createContent(data = contentData)
      whenever(contentRepository.getContent("1")).doReturn(content)

      val response = createContent(
        data = createContentData(
          id = "10"
        )
      )
      whenever(progressionRepository.updateProgression("8")).doReturn(response to false)

      viewModel.loadCollection(Data(id = "1"))

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
      viewModel.toggleEpisodeCompleted(episodeData, episodePosition)

      // Then
      verify(progressionRepository).updateProgression("8")
      verifyNoMoreInteractions(progressionRepository)

      viewModel.completionActionResult.value?.first?.peekContent() isEqualTo
          CollectionViewModel.EpisodeProgressionActionResult.EpisodeFailedToMarkComplete
      viewModel.completionActionResult.value?.second isEqualTo
          episodePosition
    }
  }

  @Test
  fun toggleEpisodeCompleted_markCompletedApiError() {
    testCoroutineRule.runBlockingTest {

      // Given
      val contentData = createContentData()
      val content = createContent(data = contentData)
      whenever(contentRepository.getContent("1")).doReturn(content)

      whenever(progressionRepository.updateProgression("8")).doThrow(IOException())

      viewModel.loadCollection(Data(id = "1"))

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
      viewModel.toggleEpisodeCompleted(episodeData, episodePosition)

      // Then
      verify(progressionRepository).updateProgression("8")
      verifyNoMoreInteractions(progressionRepository)

      viewModel.completionActionResult.value?.first?.peekContent() isEqualTo
          CollectionViewModel.EpisodeProgressionActionResult.EpisodeFailedToMarkComplete
      viewModel.completionActionResult.value?.second isEqualTo
          episodePosition
    }
  }

  @Test
  fun toggleEpisodeCompleted_markInProgressSuccess() {
    testCoroutineRule.runBlockingTest {

      // Given
      val contentData = createContentData()
      val content = createContent(data = contentData)
      whenever(contentRepository.getContent("1")).doReturn(content)

      val response = createContent(
        data = createContentData(
          id = "10"
        )
      )
      whenever(progressionRepository.updateProgression("8")).doReturn(response to true)

      viewModel.loadCollection(Data(id = "1"))

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
      viewModel.toggleEpisodeCompleted(episodeData, episodePosition)

      // Then
      verify(progressionRepository).updateProgression("8")
      verifyNoMoreInteractions(progressionRepository)

      viewModel.completionActionResult.value?.first?.peekContent() isEqualTo
          CollectionViewModel.EpisodeProgressionActionResult.EpisodeMarkedInProgress
      viewModel.completionActionResult.value?.second isEqualTo
          episodePosition
    }
  }

  @Test
  fun toggleEpisodeCompleted_markInProgressFailure() {
    testCoroutineRule.runBlockingTest {

      // Given
      val contentData = createContentData()
      val content = createContent(data = contentData)
      whenever(contentRepository.getContent("1")).doReturn(content)

      val response = createContent(
        data = createContentData(
          id = "10"
        )
      )
      whenever(progressionRepository.updateProgression("8")).doReturn(response to false)

      viewModel.loadCollection(Data(id = "1"))

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
      viewModel.toggleEpisodeCompleted(episodeData, episodePosition)

      // Then
      verify(progressionRepository).updateProgression("8")
      verifyNoMoreInteractions(progressionRepository)

      viewModel.completionActionResult.value?.first?.peekContent() isEqualTo
          CollectionViewModel.EpisodeProgressionActionResult.EpisodeFailedToMarkInProgress
      viewModel.completionActionResult.value?.second isEqualTo
          episodePosition
    }
  }

  @Test
  fun toggleEpisodeCompleted_markInProgressApiError() {
    testCoroutineRule.runBlockingTest {

      // Given
      val contentData = createContentData()
      val content = createContent(data = contentData)
      whenever(contentRepository.getContent("1")).doReturn(content)
      whenever(progressionRepository.updateProgression("8")).doThrow(IOException())

      viewModel.loadCollection(Data(id = "1"))

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
      viewModel.toggleEpisodeCompleted(episodeData, episodePosition)

      // Then
      verify(progressionRepository).updateProgression("8")
      verifyNoMoreInteractions(progressionRepository)
      viewModel.completionActionResult.value?.first?.peekContent() isEqualTo
          CollectionViewModel.EpisodeProgressionActionResult.EpisodeFailedToMarkInProgress
      viewModel.completionActionResult.value?.second isEqualTo
          episodePosition
    }
  }

  private fun createContentData(
    id: String = "1",
    type: String = "collection",
    isBookmarked: Boolean = false,
    groups: Contents? = Contents(datum = (1..2).map { Data(id = it.toString(), type = "groups") }),
    bookmark: Content? = null
  ) = Data(
    id = id,
    type = "contents",
    attributes = Attributes(contentType = type, bookmarked = isBookmarked),
    relationships = Relationships(
      groups = groups,
      bookmark = bookmark
    )
  )

  private fun removeBookmark(data: Data) = data.copy(
    attributes = data.attributes?.copy(bookmarked = false),
    relationships = data.relationships?.copy(bookmark = null)
  )

  private fun createContent(
    data: Data = createContentData(),
    included: List<Data> = emptyList()
  ): Content = Content(
    datum = data,
    included = included
  )

  private fun getIncludedDataForCollection() = listOf(
    createGroup(1, "one", 5),
    createGroup(2, "two", 7),
    createEpisode(5, "five", relationships = createRelationship()),
    createEpisode(6, "six"),
    createEpisode(7, "seven"),
    createEpisode(8, "eight"),
    Data(id = "9", type = "progressions", attributes = Attributes(percentComplete = 10.0))
  )

  private fun createRelationship() = Relationships(progression = Content(datum = Data(id = "9")))

  private fun createGroup(id: Int, name: String, dataId: Int) = Data(
    id = id.toString(),
    type = "groups",
    attributes = Attributes(name = name),
    relationships = Relationships(
      contents = Contents(
        datum = listOf(
          Data(id = dataId.toString(), type = "contents"),
          Data(id = (dataId + 1).toString(), type = "contents")
        )
      )
    )
  )

  private fun createEpisode(id: Int, name: String, relationships: Relationships? = null) = Data(
    id = id.toString(),
    type = "contents",
    attributes = Attributes(name = name),
    relationships = relationships
  )


}
