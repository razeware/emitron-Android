package com.razeware.emitron.ui.player

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.*
import com.razeware.emitron.data.createContent
import com.razeware.emitron.data.createContentData
import com.razeware.emitron.data.progressions.ProgressionRepository
import com.razeware.emitron.data.settings.SettingsRepository
import com.razeware.emitron.data.video.VideoRepository
import com.razeware.emitron.model.Content
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.Download
import com.razeware.emitron.ui.mytutorial.bookmarks.BookmarkActionDelegate
import com.razeware.emitron.utils.*
import okhttp3.ResponseBody
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Month
import retrofit2.Response

class PlayerViewModelTest {

  private val settingsRepository: SettingsRepository = mock()

  private val videoRepository: VideoRepository = mock()

  private val progressionRepository: ProgressionRepository = mock()

  private val bookmarkActionDelegate: BookmarkActionDelegate = mock()

  private val loggerImpl: LoggerImpl = mock()

  private lateinit var viewModel: PlayerViewModel

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  private fun createViewModel() {
    viewModel =
      PlayerViewModel(
        videoRepository,
        bookmarkActionDelegate,
        settingsRepository,
        progressionRepository,
        loggerImpl
      )
  }

  private fun createPlaylist(data: Data = createContentData(), currentEpisode: Data? = null) =
    Playlist(
      data,
      listOf(
        createContentData(id = "1", videoId = 1),
        createContentData(id = "2", videoId = 2),
        createContentData(id = "3", videoId = 3),
        createContentData(id = "4", videoId = 4)
      ),
      currentEpisode = currentEpisode
    )

  private fun createOfflinePlaylist() =
    Playlist(
      createContentData(
        type = "screencast"
      ),
      listOf(
        createContentData(
          id = "1", videoId = 1, download =
          Download(
            progress = 25,
            state = 3,
            failureReason = 0,
            url = "download/1"
          )
        ),
        createContentData(id = "2", videoId = 2)
      ),
      currentEpisode = createContentData(
        type = "collection",
        download = Download(
          progress = 25,
          state = 3,
          failureReason = 0,
          url = "download/1"
        )
      )
    )

  @Test
  fun startPlayback() {
    createViewModel()

    testCoroutineRule.runBlockingTest {
      whenever(videoRepository.getVideoStream("1"))
        .doReturn(createContent(createContentData(videoUrl = "TheSongOfLife")))
      whenever(videoRepository.getVideoPlaybackToken()).doReturn(
        createContent(createContentData(playbackToken = "WubbaLubbaDubDub"))
      )

      val expectedPlaylist = createPlaylist(createContentData(type = "screencast"))

      // When
      viewModel.startPlayback(expectedPlaylist)
      val playbackToken =
        viewModel.playerToken.observeForTestingResult()
      val playlist =
        viewModel.playlist.observeForTestingResult()
      val currentEpisode =
        viewModel.currentEpisode.observeForTestingResult()
      val nextEpisode =
        viewModel.nextEpisode.observeForTestingResult()

      // Then
      verify(videoRepository).getVideoStream("1")
      verify(videoRepository).getVideoPlaybackToken()
      verifyNoMoreInteractions(videoRepository)

      nextEpisode isEqualTo expectedPlaylist.episodes[1]
      currentEpisode isEqualTo createContentData(videoUrl = "TheSongOfLife")
      currentEpisode.getUrl() isEqualTo "TheSongOfLife"
      playbackToken isEqualTo "WubbaLubbaDubDub"
      playlist isEqualTo expectedPlaylist
    }
  }

  @Test
  fun startPlayback_offline() {
    createViewModel()

    testCoroutineRule.runBlockingTest {

      val expectedPlaylist = createOfflinePlaylist()

      // When
      viewModel.startPlayback(expectedPlaylist)
      val playlist =
        viewModel.playlist.observeForTestingResult()
      val currentEpisode =
        viewModel.currentEpisode.observeForTestingResult()
      val nextEpisode =
        viewModel.nextEpisode.observeForTestingResult()

      // Then
      verifyNoMoreInteractions(videoRepository)
      nextEpisode isEqualTo expectedPlaylist.episodes[1]
      currentEpisode isEqualTo createContentData(
        download = Download(
          progress = 25,
          state = 3,
          failureReason = 0,
          url = "download/1"
        )
      )
      currentEpisode.getUrl() isEqualTo "download/1"
      playlist isEqualTo expectedPlaylist
    }
  }

  @Test
  fun startPlayback_withCurrentEpisode() {
    createViewModel()

    testCoroutineRule.runBlockingTest {
      whenever(videoRepository.getVideoStream("3")).doReturn(
        createContent(
          createContentData("3", videoId = 3, videoUrl = "TheSongOfLife2")
        )
      )
      whenever(videoRepository.getVideoPlaybackToken()).doReturn(
        createContent(createContentData(playbackToken = "WubbaLubbaDubDub"))
      )
      val expectedPlaylist = createPlaylist(
        createContentData(type = "screencast"),
        currentEpisode = createContentData("3", videoId = 3)
      )

      // When
      viewModel.startPlayback(expectedPlaylist)
      val playbackToken =
        viewModel.playerToken.observeForTestingResult()
      val playlist =
        viewModel.playlist.observeForTestingResult()
      val currentEpisode =
        viewModel.currentEpisode.observeForTestingResult()
      val nextEpisode =
        viewModel.nextEpisode.observeForTestingResult()

      // Then
      verify(videoRepository).getVideoStream("3")
      verify(videoRepository).getVideoPlaybackToken()
      verifyNoMoreInteractions(videoRepository)

      nextEpisode isEqualTo expectedPlaylist.episodes[3]
      currentEpisode isEqualTo createContentData("3", videoId = 3, videoUrl = "TheSongOfLife2")
      currentEpisode.getUrl() isEqualTo "TheSongOfLife2"
      playbackToken isEqualTo "WubbaLubbaDubDub"
      playlist isEqualTo expectedPlaylist
    }
  }

  @Test
  fun startPlayback_withLastEpisode() {
    createViewModel()

    testCoroutineRule.runBlockingTest {
      whenever(videoRepository.getVideoStream("4")).doReturn(
        createContent(
          createContentData("4", videoId = 4, videoUrl = "TheSongOfLife3")
        )
      )
      whenever(videoRepository.getVideoPlaybackToken()).doReturn(
        createContent(createContentData(playbackToken = "WubbaLubbaDubDub"))
      )
      val expectedPlaylist = createPlaylist(
        createContentData(type = "screencast"),
        currentEpisode = createContentData("4", videoId = 4)
      )

      // When
      viewModel.startPlayback(expectedPlaylist)
      val playbackToken =
        viewModel.playerToken.observeForTestingResult()
      val playlist =
        viewModel.playlist.observeForTestingResult()
      val currentEpisode =
        viewModel.currentEpisode.observeForTestingResult()
      val nextEpisode =
        viewModel.nextEpisode.observeForTestingResultNullable()

      // Then
      verify(videoRepository).getVideoStream("4")
      verify(videoRepository).getVideoPlaybackToken()
      verifyNoMoreInteractions(videoRepository)

      nextEpisode isEqualTo null
      currentEpisode isEqualTo createContentData("4", videoId = 4, videoUrl = "TheSongOfLife3")
      currentEpisode.getUrl() isEqualTo "TheSongOfLife3"
      playbackToken isEqualTo "WubbaLubbaDubDub"
      playlist isEqualTo expectedPlaylist
    }
  }

  @Test
  fun playNextEpisode() {
    createViewModel()
    testCoroutineRule.runBlockingTest {
      whenever(videoRepository.getVideoStream("1"))
        .doReturn(createContent(createContentData(videoUrl = "TheSongOfLife")))
      whenever(videoRepository.getVideoStream("2"))
        .doReturn(
          createContent(
            createContentData("2", videoId = 2, videoUrl = "TheSongOfLife2")
          )
        )
      whenever(videoRepository.getVideoPlaybackToken()).doReturn(
        createContent(createContentData(playbackToken = "WubbaLubbaDubDub"))
      )

      val expectedPlaylist = createPlaylist(createContentData(type = "screencast"))
      viewModel.startPlayback(expectedPlaylist)

      // When
      viewModel.playNextEpisode()
      val playbackToken =
        viewModel.playerToken.observeForTestingResult()
      val playlist =
        viewModel.playlist.observeForTestingResult()
      val currentEpisode =
        viewModel.currentEpisode.observeForTestingResult()
      val nextEpisode =
        viewModel.nextEpisode.observeForTestingResult()

      // Then
      verify(videoRepository).getVideoStream("1")
      verify(videoRepository).getVideoStream("2")
      verify(videoRepository, times(2)).getVideoPlaybackToken()
      verifyNoMoreInteractions(videoRepository)

      nextEpisode isEqualTo expectedPlaylist.episodes[2]
      currentEpisode isEqualTo createContentData("2", videoId = 2, videoUrl = "TheSongOfLife2")
      currentEpisode.getUrl() isEqualTo "TheSongOfLife2"
      playbackToken isEqualTo "WubbaLubbaDubDub"
      playlist isEqualTo expectedPlaylist
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

      whenever(videoRepository.getVideoStream("1"))
        .doReturn(createContent(createContentData()))
      whenever(videoRepository.getVideoPlaybackToken()).doReturn(
        createContent(createContentData(playbackToken = "WubbaLubbaDubDub"))
      )
      val expectedPlaylist = createPlaylist(createContentData(type = "screencast"))
      viewModel.startPlayback(expectedPlaylist)

      // When
      viewModel.bookmarkActionResult.observeForTestingResultNullable()
      viewModel.updateContentBookmark()

      with(viewModel) {
        verify(bookmarkActionDelegate).bookmarkActionResult
        verify(bookmarkActionDelegate).updateContentBookmark(
          createContentData()
        )
        verifyNoMoreInteractions(bookmarkActionDelegate)
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
      whenever(videoRepository.getVideoStream("1"))
        .doReturn(createContent(createContentData()))
      whenever(videoRepository.getVideoPlaybackToken()).doReturn(
        createContent(createContentData(playbackToken = "WubbaLubbaDubDub"))
      )
      val expectedPlaylist = createPlaylist(createContentData(type = "screencast"))
      viewModel.startPlayback(expectedPlaylist)

      // When
      viewModel.bookmarkActionResult.observeForTestingResultNullable()
      viewModel.updateContentBookmark()

      with(viewModel) {
        verify(bookmarkActionDelegate).bookmarkActionResult
        verify(bookmarkActionDelegate).updateContentBookmark(
          createContentData()
        )
        verifyNoMoreInteractions(bookmarkActionDelegate)
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
      whenever(videoRepository.getVideoStream("1"))
        .doReturn(createContent(createContentData()))
      whenever(videoRepository.getVideoPlaybackToken()).doReturn(
        createContent(createContentData(playbackToken = "WubbaLubbaDubDub"))
      )
      val expectedPlaylist = createPlaylist(createContentData(type = "screencast"))
      viewModel.startPlayback(expectedPlaylist)

      // When
      viewModel.bookmarkActionResult.observeForTestingResultNullable()
      viewModel.updateContentBookmark()

      // Then
      with(viewModel) {
        verify(bookmarkActionDelegate).bookmarkActionResult
        verify(bookmarkActionDelegate).updateContentBookmark(
          createContentData()
        )
        verifyNoMoreInteractions(bookmarkActionDelegate)
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
      // Given
      whenever(videoRepository.getVideoStream("1"))
        .doReturn(createContent(createContentData()))
      whenever(videoRepository.getVideoPlaybackToken()).doReturn(
        createContent(createContentData(playbackToken = "WubbaLubbaDubDub"))
      )
      val expectedPlaylist = createPlaylist(createContentData(type = "screencast"))
      viewModel.startPlayback(expectedPlaylist)

      // When
      viewModel.bookmarkActionResult.observeForTestingResultNullable()
      viewModel.updateContentBookmark()

      // Then
      with(viewModel) {
        verify(bookmarkActionDelegate).bookmarkActionResult
        verify(bookmarkActionDelegate).updateContentBookmark(
          createContentData()
        )
        verifyNoMoreInteractions(bookmarkActionDelegate)
        bookmarkActionResult.value?.peekContent() isEqualTo
            BookmarkActionDelegate.BookmarkActionResult.BookmarkFailedToDelete
      }
    }
  }

  @Test
  fun updateAutoPlayNext() {
    createViewModel()
    viewModel.updateAutoPlayNext(true)
    verify(settingsRepository).updateAutoPlaybackAllowed(true)
    verifyNoMoreInteractions(settingsRepository)
  }

  @Test
  fun updatePlaybackSpeed() {
    createViewModel()
    viewModel.updatePlaybackSpeed(1.0f)
    verify(settingsRepository).updateSelectedPlaybackSpeed(1.0f)
    verifyNoMoreInteractions(settingsRepository)
  }

  @Test
  fun updatePlaybackQuality() {
    createViewModel()
    viewModel.updatePlaybackQuality(1)
    verify(settingsRepository).updateSelectedPlaybackQuality(1)
    verifyNoMoreInteractions(settingsRepository)
  }

  @Test
  fun updateSubtitleLanguage() {
    createViewModel()
    viewModel.updateSubtitleLanguage("en")
    verify(settingsRepository).updateSubtitleLanguage("en")
    verifyNoMoreInteractions(settingsRepository)
  }

  @Test
  fun shouldAutoPlay() {
    createViewModel()
    viewModel.updateSubtitleLanguage("en")
    verify(settingsRepository).updateSubtitleLanguage("en")
    verifyNoMoreInteractions(settingsRepository)
  }

  @Test
  fun getSubtitleLanguage() {
    createViewModel()
    viewModel.updateSubtitleLanguage("en")
    verify(settingsRepository).updateSubtitleLanguage("en")
    verifyNoMoreInteractions(settingsRepository)
  }

  @Test
  fun getPlaybackQuality() {
    createViewModel()
    whenever(settingsRepository.getPlaybackQuality()).doReturn(1)
    val result = viewModel.getPlaybackQuality()
    result isEqualTo 1
    verify(settingsRepository).getPlaybackQuality()
    verifyNoMoreInteractions(settingsRepository)
  }

  @Test
  fun getPlaybackSpeed() {
    createViewModel()
    whenever(settingsRepository.getPlaybackSpeed()).doReturn(1.0f)
    val result = viewModel.getPlaybackSpeed()
    result isEqualTo 1.0f
    verify(settingsRepository).getPlaybackSpeed()
    verifyNoMoreInteractions(settingsRepository)
  }

  @Test
  fun getNowPlayingTitle() {
    createViewModel()

    testCoroutineRule.runBlockingTest {

      // Given
      whenever(videoRepository.getVideoStream("1"))
        .doReturn(createContent(createContentData(videoUrl = "TheSongOfLife")))
      whenever(videoRepository.getVideoPlaybackToken()).doReturn(
        createContent(createContentData(playbackToken = "WubbaLubbaDubDub"))
      )

      val expectedPlaylist = createPlaylist(createContentData(type = "screencast"))
      viewModel.startPlayback(expectedPlaylist)

      // When
      val result = viewModel.getNowPlayingTitle()

      // Then
      result isEqualTo "Introduction to Kotlin Lambdas"
    }
  }

  @Test
  fun getNowPlayingDescription() {
    createViewModel()
    testCoroutineRule.runBlockingTest {

      // Given
      whenever(videoRepository.getVideoStream("1"))
        .doReturn(createContent(createContentData(videoUrl = "TheSongOfLife")))
      whenever(videoRepository.getVideoPlaybackToken()).doReturn(
        createContent(createContentData(playbackToken = "WubbaLubbaDubDub"))
      )

      val expectedPlaylist = createPlaylist(createContentData(type = "screencast"))
      viewModel.startPlayback(expectedPlaylist)

      // When
      val result = viewModel.getNowPlayingDescription()

      // Then
      result isEqualTo "Lambda expression is simplified representation of a function."
    }
  }

  @Test
  fun getNowPlayingCoverArt() {
    createViewModel()
    testCoroutineRule.runBlockingTest {

      // Given
      whenever(videoRepository.getVideoStream("1"))
        .doReturn(createContent(createContentData(videoUrl = "TheSongOfLife")))
      whenever(videoRepository.getVideoPlaybackToken()).doReturn(
        createContent(createContentData(playbackToken = "WubbaLubbaDubDub"))
      )

      val expectedPlaylist = createPlaylist(createContentData(type = "screencast"))
      viewModel.startPlayback(expectedPlaylist)

      // When
      val result = viewModel.getNowPlayingCoverArt()

      // Then
      result isEqualTo "https://koenig-media.razeware.com/KotlinLambdas-feature.png"
    }
  }

  @Test
  fun updateProgress() {
    createViewModel()

    testCoroutineRule.runBlockingTest {
      // Given
      val today = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)
      whenever(videoRepository.getVideoStream("1"))
        .doReturn(createContent(createContentData(videoUrl = "TheSongOfLife")))
      whenever(videoRepository.getVideoPlaybackToken()).doReturn(
        createContent(createContentData(playbackToken = "WubbaLubbaDubDub"))
      )
      whenever(
        progressionRepository.updatePlaybackProgress(
          "WubbaLubbaDubDub",
          "1",
          5L,
          5L
        )
      ).doReturn(Response.success(createContent()))
      viewModel.startPlayback(createPlaylist(createContentData(type = "screencast")))

      // When
      viewModel.updateProgress(true, 5002, today)


      verify(videoRepository).getVideoStream("1")
      verify(videoRepository).getVideoPlaybackToken()
      verify(progressionRepository).updatePlaybackProgress(
        "WubbaLubbaDubDub",
        "1",
        5L,
        5L
      )
      verifyNoMoreInteractions(videoRepository)

      viewModel.updateProgress(true, 5000, today)
      verifyNoMoreInteractions(videoRepository)
    }
  }

  @Test
  fun updateProgress_resetToken() {
    createViewModel()

    testCoroutineRule.runBlockingTest {
      // Given
      val today = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)
      whenever(videoRepository.getVideoStream("1"))
        .doReturn(createContent(createContentData(videoUrl = "TheSongOfLife")))
      whenever(videoRepository.getVideoPlaybackToken()).doReturn(
        createContent(createContentData(playbackToken = "WubbaLubbaDubDub"))
      )
      val responseBody: ResponseBody = mock()
      val response = Response.error<Content>(400, responseBody)
      whenever(
        progressionRepository.updatePlaybackProgress(
          "WubbaLubbaDubDub",
          "1",
          5L,
          5L
        )
      ).doReturn(response)
      viewModel.startPlayback(createPlaylist(createContentData(type = "screencast")))

      // When
      viewModel.updateProgress(true, 5002, today)
      val resetPlaybackToken = viewModel.resetPlaybackToken.observeForTestingResult()

      verify(videoRepository).getVideoStream("1")
      verify(videoRepository).getVideoPlaybackToken()
      verify(progressionRepository).updatePlaybackProgress(
        "WubbaLubbaDubDub",
        "1",
        5L,
        5L
      )
      verifyNoMoreInteractions(videoRepository)
      resetPlaybackToken isEqualTo true
    }
  }

  @Test
  fun updateProgress_updateLocal() {
    createViewModel()

    testCoroutineRule.runBlockingTest {
      // Given
      val today = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)
      viewModel.startPlayback(
        createPlaylist(
          createContentData(
            type = "screencast",
            download = Download(
              progress = 100,
              state = 3,
              failureReason = 0,
              url = "download/1"
            )
          ),
          currentEpisode = createContentData(
            type = "screencast",
            download = Download(
              progress = 100,
              state = 3,
              failureReason = 0,
              url = "download/1"
            )
          )
        )
      )

      // When
      viewModel.updateProgress(false, 5002, today)

      val enqueueOfflineProgressUpdate =
        viewModel.enqueueOfflineProgressUpdate.observeForTestingResult()

      // Then
      verify(progressionRepository).updateLocalProgression(
        contentId = "1",
        percentComplete = 50,
        progress = 5L,
        finished = false,
        synced = false,
        updatedAt = today
      )
      verify(progressionRepository).updateWatchStat(
        contentId = "1",
        duration = 0L,
        watchedAt = today
      )
      verifyNoMoreInteractions(progressionRepository)

      enqueueOfflineProgressUpdate isEqualTo "1"
    }
  }

  @Test
  fun updateProgress_updateLocal_updateWatchStat() {
    createViewModel()

    testCoroutineRule.runBlockingTest {
      // Given
      val today = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)
      viewModel.startPlayback(
        createPlaylist(
          createContentData(
            type = "screencast",
            download = Download(
              progress = 100,
              state = 3,
              failureReason = 0,
              url = "download/1"
            )
          ),
          currentEpisode = createContentData(
            type = "screencast",
            download = Download(
              progress = 100,
              state = 3,
              failureReason = 0,
              url = "download/1"
            )
          )
        )
      )

      // When
      viewModel.updateProgress(false, 5002, today)
      viewModel.updateProgress(false, 10002, today)

      val enqueueOfflineProgressUpdate =
        viewModel.enqueueOfflineProgressUpdate.observeForTestingResult()

      // Then
      verify(progressionRepository).updateLocalProgression(
        contentId = "1",
        percentComplete = 50,
        progress = 5L,
        finished = false,
        synced = false,
        updatedAt = today
      )
      verify(progressionRepository).updateLocalProgression(
        contentId = "1",
        percentComplete = 100,
        progress = 10L,
        finished = true,
        synced = false,
        updatedAt = today
      )
      verify(progressionRepository).updateWatchStat(
        contentId = "1",
        duration = 0L,
        watchedAt = today
      )
      verify(progressionRepository).updateWatchStat(
        contentId = "1",
        duration = 5L,
        watchedAt = today
      )
      verifyNoMoreInteractions(progressionRepository)

      enqueueOfflineProgressUpdate isEqualTo "1"
    }
  }

  @Test
  fun resumePlayback_success() {
    createViewModel()

    testCoroutineRule.runBlockingTest {
      // Given
      whenever(videoRepository.getVideoStream("1"))
        .doReturn(createContent(createContentData(videoUrl = "TheSongOfLife")))
      whenever(videoRepository.getVideoPlaybackToken()).doReturn(
        createContent(createContentData(playbackToken = "WubbaLubbaDubDub"))
      )
      viewModel.startPlayback(createPlaylist(createContentData(type = "screencast")))

      whenever(videoRepository.getVideoPlaybackToken()).doReturn(
        createContent(createContentData(playbackToken = "WubbaLubbaDubDubAgain"))
      )
      // When
      viewModel.resumePlayback()
      val resetPlaybackToken = viewModel.resetPlaybackToken.observeForTestingResult()
      val playbackToken = viewModel.playerToken.observeForTestingResult()

      verify(videoRepository).getVideoStream("1")
      verify(videoRepository, times(2)).getVideoPlaybackToken()
      verifyNoMoreInteractions(videoRepository)

      // Then
      resetPlaybackToken isEqualTo false
      playbackToken isEqualTo "WubbaLubbaDubDubAgain"
    }
  }

  @Test
  fun resumePlayback_failure() {
    createViewModel()
    testCoroutineRule.runBlockingTest {
      // When

      whenever(videoRepository.getVideoPlaybackToken()).doReturn(null)
      viewModel.resumePlayback()
      val resetPlaybackToken = viewModel.resetPlaybackToken.observeForTestingResult()
      val playbackToken = viewModel.playerToken.observeForTestingResultNullable()

      // Then
      verify(videoRepository).getVideoPlaybackToken()
      verifyNoMoreInteractions(videoRepository)

      resetPlaybackToken isEqualTo false
      playbackToken isEqualTo null
    }
  }

  @Test
  fun isContentTypeScreencast() {
    createViewModel()
    testCoroutineRule.runBlockingTest {
      whenever(videoRepository.getVideoStream("1")).doReturn(createContent())
      whenever(videoRepository.getVideoPlaybackToken()).doReturn(createContent())

      // When
      viewModel.startPlayback(createPlaylist(createContentData(type = "screencast")))

      // Then
      viewModel.isContentTypeScreencast() isEqualTo true
    }
  }

  @Test
  fun hasMoreEpisodes() {
    createViewModel()
    testCoroutineRule.runBlockingTest {
      whenever(videoRepository.getVideoStream(anyString())).doReturn(createContent())
      whenever(videoRepository.getVideoPlaybackToken()).doReturn(createContent())

      // When
      viewModel.startPlayback(createPlaylist(createContentData()))
      viewModel.playNextEpisode()
      viewModel.playNextEpisode()


      viewModel.hasMoreEpisodes() isEqualTo true
      viewModel.playNextEpisode()

      // Then
      viewModel.hasMoreEpisodes() isEqualTo false
    }
  }

  @Test
  fun getAllEpisodes() {
    createViewModel()
    testCoroutineRule.runBlockingTest {
      whenever(videoRepository.getVideoStream(anyString())).doReturn(createContent())
      whenever(videoRepository.getVideoPlaybackToken()).doReturn(createContent())

      // When
      viewModel.startPlayback(createPlaylist(createContentData()))

      viewModel.getAllEpisodes() isEqualTo listOf(
        createContentData(id = "1", videoId = 1),
        createContentData(id = "2", videoId = 2),
        createContentData(id = "3", videoId = 3),
        createContentData(id = "4", videoId = 4)
      )
    }
  }
}
