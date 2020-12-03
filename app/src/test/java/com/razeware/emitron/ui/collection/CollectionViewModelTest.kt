package com.razeware.emitron.ui.collection

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import com.raywenderlich.android.inappreview.InAppReviewView
import com.razeware.emitron.data.*
import com.razeware.emitron.data.content.ContentRepository
import com.razeware.emitron.data.login.LoginRepository
import com.razeware.emitron.model.*
import com.razeware.emitron.ui.download.DownloadActionDelegate
import com.razeware.emitron.ui.login.PermissionActionDelegate
import com.razeware.emitron.ui.mytutorial.bookmarks.BookmarkActionDelegate
import com.razeware.emitron.ui.mytutorial.progressions.ProgressionActionDelegate
import com.razeware.emitron.ui.onboarding.OnboardingActionDelegate
import com.razeware.emitron.ui.player.Playlist
import com.razeware.emitron.utils.*
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

  private val inAppReviewView: InAppReviewView = mock()

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

    viewModel.setInAppReviewView(inAppReviewView)
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
      val expectedEpisodes = expectedEpisodeList()
      whenever(contentRepository.getContent("1")).doReturn(content)

      val data = Data(id = "1", attributes = Attributes(contentType = "collection"))

      viewModel.collection.observeForTestingResultNullable()
      val uiStateObserver = viewModel.uiState.observeForTestingObserver()

      viewModel.loadCollection(data)

      assertThat(viewModel.collection.value).isEqualTo(contentData)
      assertThat(viewModel.getCollectionEpisodes()).isEqualTo(expectedEpisodes)


      verify(uiStateObserver).onChanged(UiStateManager.UiState.LOADING)
      verify(uiStateObserver).onChanged(UiStateManager.UiState.LOADED)
      verifyNoMoreInteractions(uiStateObserver)
    }
  }

  @Test
  fun loadCollectionOffline() {
    createViewModel()

    testCoroutineRule.runBlockingTest {
      val contentData = createContentData(download = Download(cached = true))
      val content = createContent(data = contentData, included = getIncludedDataForCollection())
      val expectedEpisodes = expectedEpisodeList()
      whenever(contentRepository.getContentFromDb("1")).doReturn(content)

      val data = Data(
        id = "1", attributes = Attributes(contentType = "collection"),
        download = Download(progress = 100)
      )

      viewModel.collection.observeForTestingResultNullable()
      val uiStateObserver = viewModel.uiState.observeForTestingObserver()

      viewModel.loadCollection(data)

      assertThat(viewModel.collection.value).isEqualTo(contentData)
      assertThat(viewModel.getCollectionEpisodes()).isEqualTo(expectedEpisodes)

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
      val uiStateObserver = viewModel.uiState.observeForTestingObserver()

      viewModel.loadCollection(data)

      assertThat(viewModel.collection.value).isEqualTo(contentData)
      assertThat(viewModel.getCollectionEpisodes()).isEqualTo(emptyList<CollectionEpisode>())


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
      val uiStateObserver = viewModel.uiState.observeForTestingObserver()
      val collectionObserver = viewModel.collection.observeForTestingObserver()
      // When
      viewModel.loadCollection(data)

      // Then
      assertThat(viewModel.collection.value).isEqualTo(data)
      assertThat(viewModel.getCollectionEpisodes()).isEqualTo(emptyList<CollectionEpisode>())
      verify(collectionObserver).onChanged(data)
      verifyNoMoreInteractions(collectionObserver)
      verify(uiStateObserver).onChanged(UiStateManager.UiState.LOADING)
      verify(uiStateObserver).onChanged(UiStateManager.UiState.LOADED)
      verifyNoMoreInteractions(uiStateObserver)
    }
  }

  /**
   * Test getting playlist when content type is collection
   */
  @Test
  fun getPlaylist_withProgress() {
    createViewModel()
    testCoroutineRule.runBlockingTest {
      val contentData = createContentData()
      val content = createContent(data = contentData, included = getIncludedDataForCollection())
      // Given
      val inProgressEpisode = Data(
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
      val expectedPlaylist =
        Playlist(
          contentData,
          episodes = listOf(
            inProgressEpisode,
            Data(
              id = "6", type = "contents",
              attributes = Attributes(name = "six"),
              relationships = Relationships()
            ),
            Data(
              id = "7", type = "contents",
              attributes = Attributes(name = "seven"),
              relationships = Relationships()
            ),
            Data(
              id = "8", type = "contents",
              attributes = Attributes(name = "eight"),
              relationships = Relationships()
            )
          ),
          currentEpisode = inProgressEpisode
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
      val contentData = createContent()
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
      val downloadProgress = DownloadProgress(
        "1",
        25,
        DownloadState.COMPLETED
      )
      viewModel.updateDownload(downloadProgress)
      verify(downloadActionDelegate).updateDownloadProgress(downloadProgress)
    }
  }

  @Test
  fun updateCollectionDownloadState() {
    createViewModel()
    testCoroutineRule.runBlockingTest {
      val contentData = createContent()
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
          state = DownloadState.COMPLETED.ordinal,
          cached = true
        )
      )
      val content = createContent(data = contentData)
      whenever(contentRepository.getContentFromDb("1")).doReturn(content)
      whenever(contentRepository.getContent("1")).doReturn(content)
      viewModel.loadCollection(Data(id = "1"))
      whenever(permissionActionDelegate.isDownloadAllowed()).doReturn(true)

      // When
      val result = viewModel.isContentPlaybackAllowed(true, checkDownloadPermission = true)

      // Then
      result isEqualTo true
      verify(contentRepository).getContentFromDb("1")
      verifyNoMoreInteractions(contentRepository)
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

  @Test
  fun getProgress() {
    createViewModel()
    testCoroutineRule.runBlockingTest {
      val contentData = createContentData(
        progression = withRelatedProgression(withProgression(45.0, false))
      )
      val content = createContent(
        data = contentData,
        included = getIncludedDataForCollection()
      )
      // Given
      whenever(contentRepository.getContent("1")).doReturn(content)

      viewModel.loadCollection(Data(id = "1"))

      // When
      val result = viewModel.hasProgress()

      // Then
      assertThat(result).isEqualTo(true)
    }
  }

  @Test
  fun hasProgress() {
    createViewModel()
    testCoroutineRule.runBlockingTest {
      val contentData = createContentData()
      val content = createContent(data = contentData, included = getIncludedDataForCollection())
      // Given
      whenever(contentRepository.getContent("1")).doReturn(content)

      viewModel.loadCollection(Data(id = "1"))

      // When
      val result = viewModel.getProgress()

      // Then
      assertThat(result).isEqualTo(10)
    }
  }

  @Test
  fun updateCollectionProgressionState() {
    createViewModel()
    testCoroutineRule.runBlockingTest {
      val content =
        createContent(
          data = createContentData(),
          included = getIncludedDataForCollection()
        )
      // Given
      whenever(contentRepository.getContent("1")).doReturn(content)

      viewModel.loadCollection(Data(id = "1"))

      viewModel.completionActionResult.observeForTestingResultNullable()
      viewModel.updateCollectionProgressionState()

      viewModel.collection.value?.isProgressionFinished() isEqualTo false
    }
  }

  @Test
  fun removeEpisodeDownload() {
    createViewModel()
    testCoroutineRule.runBlockingTest {
      val content =
        createContent(
          data = createContentData(),
          included = getIncludedDataForCollection()
        )
      // Given
      whenever(contentRepository.getContent("1")).doReturn(content)

      viewModel.loadCollection(Data(id = "1"))

      viewModel.removeEpisodeDownload("5")

      viewModel.getCollectionEpisodes()[1].data?.isDownloaded() isEqualTo false
    }
  }

  @Test
  fun updateEpisodeProgression() {
    createViewModel()
    testCoroutineRule.runBlockingTest {
      val content =
        createContent(
          data = createContentData(),
          included = getIncludedDataForCollection()
        )
      // Given
      whenever(contentRepository.getContent("1")).doReturn(content)

      viewModel.loadCollection(Data(id = "1"))
      viewModel.collectionEpisodes.observeForTestingResultNullable()

      viewModel.updateEpisodeProgression(true, 1)
      viewModel.getCollectionEpisodes()[1].data?.isProgressionFinished() isEqualTo true
      viewModel.collectionEpisodes.value?.getOrNull(1)?.data?.isProgressionFinished() isEqualTo true
    }
  }

  @Test
  fun toggleEpisodeCompletion() {
    createViewModel()
    testCoroutineRule.runBlockingTest {
      val content =
        createContent(
          data = createContentData(),
          included = getIncludedDataForCollection()
        )
      // Given
      whenever(contentRepository.getContent("1")).doReturn(content)

      viewModel.loadCollection(Data(id = "1"))

      viewModel.toggleEpisodeCompletion(1)
      viewModel.getCollectionEpisodes()[1].data?.isProgressionFinished() isEqualTo true
      viewModel.toggleEpisodeCompletion(1)
      viewModel.getCollectionEpisodes()[1].data?.isProgressionFinished() isEqualTo false
    }
  }

  @Test
  fun updateEpisodeDownloadProgress() {
    createViewModel()
    testCoroutineRule.runBlockingTest {
      val content =
        createContent(
          data = createContentData(),
          included = getIncludedDataForCollection()
        )
      // Given
      whenever(contentRepository.getContent("1")).doReturn(content)

      viewModel.loadCollection(Data(id = "1"))

      viewModel.updateEpisodeDownloadProgress(
        listOf(
          com.razeware.emitron.model.entity.Download(
            downloadId = "5",
            progress = 50,
            state = 2
          )
        )
      )

      viewModel.getCollectionEpisodes()[1].data?.isDownloading() isEqualTo true
      viewModel.getCollectionEpisodes()[1].data?.getDownloadProgress() isEqualTo 50
    }
  }

  @Test
  fun getCollectionEpisodes() {
    createViewModel()
    testCoroutineRule.runBlockingTest {
      val content =
        createContent(
          data = createContentData(),
          included = getIncludedDataForCollection()
        )
      // Given
      whenever(contentRepository.getContent("1")).doReturn(content)

      viewModel.loadCollection(Data(id = "1"))

      viewModel.getCollectionEpisodes() isEqualTo expectedEpisodeList()
    }
  }

  private fun expectedEpisodeList() = listOf(
    CollectionEpisode(title = "one"),
    CollectionEpisode(
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
      ),
      position = 1
    ),
    CollectionEpisode(
      data = Data(
        id = "6", type = "contents",
        attributes = Attributes(name = "six"),
        relationships = Relationships()
      ),
      position = 2
    ),
    CollectionEpisode(
      title = "two",
      position = 2
    ),
    CollectionEpisode(
      data = Data(
        id = "7", type = "contents",
        attributes = Attributes(name = "seven"),
        relationships = Relationships()
      ),
      position = 3
    ),
    CollectionEpisode(
      data = Data(
        id = "8", type = "contents",
        attributes = Attributes(name = "eight"),
        relationships = Relationships()
      ),
      position = 4
    )
  )
}
