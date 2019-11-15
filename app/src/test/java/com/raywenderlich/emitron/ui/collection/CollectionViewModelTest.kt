package com.raywenderlich.emitron.ui.collection

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import com.raywenderlich.emitron.data.*
import com.raywenderlich.emitron.data.content.ContentRepository
import com.raywenderlich.emitron.data.login.LoginRepository
import com.raywenderlich.emitron.model.*
import com.raywenderlich.emitron.ui.download.DownloadActionDelegate
import com.raywenderlich.emitron.ui.login.PermissionActionDelegate
import com.raywenderlich.emitron.ui.mytutorial.bookmarks.BookmarkActionDelegate
import com.raywenderlich.emitron.ui.mytutorial.progressions.ProgressionActionDelegate
import com.raywenderlich.emitron.ui.onboarding.OnboardingActionDelegate
import com.raywenderlich.emitron.ui.player.Playlist
import com.raywenderlich.emitron.utils.*
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Month
import java.io.IOException

class CollectionViewModelTest {

  private val contentRepository: ContentRepository = mock()

  private val bookmarkActionDelegate: BookmarkActionDelegate = mock()

  private val progressionActionDelegate: ProgressionActionDelegate = mock()

  private val downloadActionDelegate: DownloadActionDelegate = mock()

  private val onboardingActionDelegate: OnboardingActionDelegate = mock()

  private val permissionActionDelegate: PermissionActionDelegate = mock()

  private lateinit var viewModel: CollectionViewModel

  private val loginRepository: LoginRepository = mock()

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  private fun createViewModel() {
    viewModel =
      CollectionViewModel(
        contentRepository,
        bookmarkActionDelegate,
        progressionActionDelegate,
        downloadActionDelegate,
        onboardingActionDelegate,
        permissionActionDelegate
      )
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

  @Test
  fun loadCollectionOffline() {
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
      whenever(contentRepository.getContentFromDb("1")).doReturn(content)

      val data = Data(
        id = "1", attributes = Attributes(contentType = "collection"),
        download = Download(progress = 100)
      )

      viewModel.collection.observeForTestingResultNullable()
      viewModel.collectionEpisodes.observeForTestingResultNullable()
      val uiStateObserver = viewModel.uiState.observeForTestingObserver()

      viewModel.loadCollection(data)

      assertThat(viewModel.collection.value).isEqualTo(contentData)
      assertThat(viewModel.collectionEpisodes.value).isEqualTo(expectedEpisodes)

      verify(contentRepository).getContentFromDb("1")
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
      val contentData = createContentData()
      val content = createContent(data = contentData, included = getIncludedDataForCollection())
      // Given
      val expectedPlaylist =
        Playlist(
          contentData,
          episodes = listOf(
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
        )

      whenever(contentRepository.getContent("1")).doReturn(content)

      viewModel.loadCollection(Data(id = "1"))

      // When
      val result = viewModel.getPlaylist()

      // Then
      assertThat(result).isEqualTo(expectedPlaylist)
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
      assertThat(result).isEqualTo(
        Playlist(
          collection = contentData,
          episodes = listOf(contentData)
        )
      )
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
      whenever(contentRepository.getContent("1")).doReturn(
        Content(
          datum = createContentData()
        )
      )
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
      whenever(contentRepository.getContent("1")).doReturn(
        Content(
          datum = createContentData()
        )
      )
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

      // Given
      val day = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)
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
      viewModel.updateContentProgression(true, episodeData, episodePosition, day)

      // Then
      verify(progressionActionDelegate).completionActionResult
      verify(progressionActionDelegate).updateContentProgression(
        true,
        episodeData, episodePosition,
        updatedAt = day
      )
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
      val day = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)

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
      viewModel.updateContentProgression(true, episodeData, episodePosition, updatedAt = day)

      // Then
      verify(progressionActionDelegate).completionActionResult
      verify(progressionActionDelegate).updateContentProgression(
        true,
        episodeData,
        episodePosition,
        updatedAt = day
      )
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
      val day = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)
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
      viewModel.updateContentProgression(true, episodeData, episodePosition, day)

      // Then
      verify(progressionActionDelegate).completionActionResult
      verify(progressionActionDelegate).updateContentProgression(
        true,
        episodeData, episodePosition,
        updatedAt = day
      )
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
      val day = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)
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
      viewModel.updateContentProgression(true, episodeData, episodePosition, day)

      // Then
      verify(progressionActionDelegate).completionActionResult
      verify(progressionActionDelegate).updateContentProgression(
        true,
        episodeData, episodePosition,
        updatedAt = day
      )
      verifyNoMoreInteractions(progressionActionDelegate)
      viewModel.completionActionResult.value?.first?.peekContent() isEqualTo
          ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeFailedToMarkInProgress
      viewModel.completionActionResult.value?.second isEqualTo
          episodePosition
    }
  }

  @Test
  fun getContentId() {
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
      val result = viewModel.getContentId()
      result isEqualTo "1"
    }
  }

  @Test
  fun getContentIds() {
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

      val result = viewModel.getContentIds()

      result isEqualTo listOf("1")
    }
  }

  @Test
  fun getContentIds_collection() {
    createViewModel()
    testCoroutineRule.runBlockingTest {
      // Given
      val contentData = com.raywenderlich.emitron.data.createContent()
      whenever(contentRepository.getContent("1")).doReturn(contentData)
      viewModel.loadCollection(Data(id = "1"))

      val result = viewModel.getContentIds()

      result isEqualTo listOf("1")
    }
  }

  @Test
  fun updateDownload() {
    createViewModel()
    testCoroutineRule.runBlockingTest {
      viewModel.updateDownload("1", 25, DownloadState.PAUSED)
      verify(downloadActionDelegate).updateDownloadProgress(
        "1", 25,
        DownloadState.PAUSED
      )
    }
  }

  @Test
  fun updateCollectionDownloadState() {
    createViewModel()
    testCoroutineRule.runBlockingTest {
      val contentData = com.raywenderlich.emitron.data.createContent()
      whenever(contentRepository.getContent("1")).doReturn(contentData)
      viewModel.loadCollection(Data(id = "1"))

      val expected = listOf(createDownload())
      viewModel.updateCollectionDownloadState(expected, listOf("1"))
      verify(downloadActionDelegate).getCollectionDownloadState(
        contentData.datum,
        expected,
        downloadIds = listOf("1")
      )
    }
  }

  @Test
  fun isContentPlaybackAllowed_Professional_isConnected() {
    createViewModel()
    testCoroutineRule.runBlockingTest {
      // Given
      val contentData = createContentData(
        type = "screencast",
        groups = null,
        professional = true
      )
      val content = createContent(data = contentData)
      whenever(contentRepository.getContent("1")).doReturn(content)
      viewModel.loadCollection(Data(id = "1"))
      whenever(permissionActionDelegate.isProfessionalVideoPlaybackAllowed()).doReturn(true)
      whenever(permissionActionDelegate.isProfessionalVideoPlaybackAllowed()).doReturn(true)

      // When
      val result = viewModel.isContentPlaybackAllowed(true)

      // Then
      result isEqualTo true
    }
  }

  @Test
  fun isContentPlaybackAllowed_isConnected_isDownloaded() {
    createViewModel()
    testCoroutineRule.runBlockingTest {
      // Given
      val contentData = createContentData(
        type = "screencast",
        groups = null,
        professional = true,
        download = Download(
          state = DownloadState.COMPLETED.ordinal
        )
      )
      val content = createContent(data = contentData)
      whenever(contentRepository.getContent("1")).doReturn(content)
      viewModel.loadCollection(Data(id = "1"))
      whenever(permissionActionDelegate.isDownloadAllowed()).doReturn(true)

      // When
      val result = viewModel.isContentPlaybackAllowed(true, checkDownloadPermission = true)

      // Then
      result isEqualTo true
    }
  }

  @Test
  fun isContentPlaybackAllowed_isOffline() {
    createViewModel()
    testCoroutineRule.runBlockingTest {
      // Given
      val contentData = createContentData(
        type = "screencast",
        groups = null,
        professional = false,
        download = Download(
          state = DownloadState.COMPLETED.ordinal
        )
      )
      val content = createContent(data = contentData)
      whenever(contentRepository.getContent("1")).doReturn(content)
      viewModel.loadCollection(Data(id = "1"))
      whenever(loginRepository.isDownloadAllowed()).doReturn(false)

      // When
      val result = viewModel.isContentPlaybackAllowed(false)

      // Then
      result isEqualTo false
    }
  }

  @Test
  fun removeDownload() {
    createViewModel()
    testCoroutineRule.runBlockingTest {
      // Given
      val contentData = createContentData(
        type = "screencast",
        groups = null,
        professional = false,
        download = Download(
          state = DownloadState.COMPLETED.ordinal
        )
      )
      val content = createContent(data = contentData)
      whenever(contentRepository.getContent("1")).doReturn(content)
      viewModel.loadCollection(Data(id = "1"))
      whenever(loginRepository.isDownloadAllowed()).doReturn(false)

      // When
      viewModel.removeDownload()

      // Then
      viewModel.isDownloaded() isEqualTo false
    }
  }
}
