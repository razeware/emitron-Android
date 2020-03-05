package com.razeware.emitron.data.progressions

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.*
import com.razeware.emitron.data.content.ContentDataSourceLocal
import com.razeware.emitron.model.Content
import com.razeware.emitron.model.Contents
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.PlaybackProgress
import com.razeware.emitron.model.entity.Progression
import com.razeware.emitron.utils.TestCoroutineRule
import com.razeware.emitron.utils.async.ThreadManager
import com.razeware.emitron.utils.isEqualTo
import kotlinx.coroutines.Dispatchers
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Month
import retrofit2.Response

class ProgressionRepositoryTest {

  private lateinit var repository: ProgressionRepository

  private val progressionApi: ProgressionApi = mock()

  private val threadManager: ThreadManager = mock()

  private val contentDataSourceLocal: ContentDataSourceLocal = mock()

  private val progressionDataSourceLocal: ProgressionDataSourceLocal = mock()


  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  @Before
  fun setUp() {
    whenever(threadManager.io).doReturn(Dispatchers.Unconfined)
    whenever(threadManager.db).doReturn(Dispatchers.Unconfined)
    repository = ProgressionRepository(
      progressionApi,
      threadManager,
      contentDataSourceLocal,
      progressionDataSourceLocal
    )
  }

  @Test
  fun updateProgression() {
    testCoroutineRule.runBlockingTest {
      val day = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)

      val expectedContent = Contents()
      whenever(progressionApi.updateProgression(any())).doReturn(expectedContent)
      val result = repository.updateProgressions(
        "1",
        true,
        day
      )
      verify(progressionApi).updateProgression(
        Contents(
          listOf(Data.newProgression("1", finished = true, updatedAt = day))
        )
      )
      Truth.assertThat(result).isEqualTo(expectedContent)
      verifyNoMoreInteractions(progressionApi)
    }
  }

  @Test
  fun updateProgression_failure() {
    testCoroutineRule.runBlockingTest {

      val day = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)
      whenever(progressionApi.updateProgression(any())).doReturn(null)

      val result =
        repository.updateProgressions("1", true, day)

      verify(progressionApi).updateProgression(
        Contents(
          listOf(Data.newProgression("1", finished = true, updatedAt = day))
        )
      )
      Truth.assertThat(result).isNull()
      verifyNoMoreInteractions(progressionApi)
    }
  }

  @Test
  fun deleteProgression() {
    testCoroutineRule.runBlockingTest {
      whenever(progressionApi.deleteProgression(anyString()))
        .doReturn(Response.success(200, Any()))

      val result = repository.deleteProgression("1")
      verify(progressionApi).deleteProgression("1")
      Truth.assertThat(result).isTrue()
      verifyNoMoreInteractions(progressionApi)
    }
  }

  @Test
  fun deleteProgression_failure() {
    testCoroutineRule.runBlockingTest {
      val responseBody: ResponseBody = mock()
      val errorResponse: Response<Any> = Response.error(401, responseBody)
      whenever(progressionApi.deleteProgression(anyString())).doReturn(errorResponse)

      val result2 = repository.deleteProgression("1")

      verify(progressionApi).deleteProgression("1")
      Truth.assertThat(result2).isFalse()
      verifyNoMoreInteractions(progressionApi)
    }
  }

  @Test
  fun updateLocalProgression() {
    testCoroutineRule.runBlockingTest {
      val today = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)

      repository.updateLocalProgression(
        contentId = "1",
        percentComplete = 10,
        progress = 50,
        finished = false,
        synced = true,
        updatedAt = today,
        progressionId = "1"
      )

      verify(progressionDataSourceLocal).updateProgress(
        contentId = "1",
        percentComplete = 10,
        progress = 50,
        finished = false,
        synced = true,
        updatedAt = today,
        progressionId = "1"
      )
      verifyNoMoreInteractions(contentDataSourceLocal)
    }
  }

  @Test
  fun updateLocalProgressions() {
    testCoroutineRule.runBlockingTest {

      val progressions = listOf(
        Progression(contentId = "1", progressionId = "1", percentComplete = 99, finished = true),
        Progression(contentId = "2", progressionId = "2", percentComplete = 50, finished = false)
      )
      repository.updateLocalProgressions(progressions)

      verify(progressionDataSourceLocal).updateLocalProgressions(progressions)
      verifyNoMoreInteractions(progressionDataSourceLocal)
    }
  }

  @Test
  fun getLocalProgressions() {
    testCoroutineRule.runBlockingTest {

      val progressions = listOf(
        Progression(contentId = "1", progressionId = "1", percentComplete = 99, finished = true),
        Progression(contentId = "2", progressionId = "2", percentComplete = 50, finished = false)
      )
      whenever(progressionDataSourceLocal.getLocalProgressions()).doReturn(
        progressions
      )
      val result = repository.getLocalProgressions()
      result isEqualTo progressions
      verify(progressionDataSourceLocal).getLocalProgressions()
      verifyNoMoreInteractions(contentDataSourceLocal)
    }
  }

  @Test
  fun updatePlaybackProgress() {
    testCoroutineRule.runBlockingTest {
      // Given
      val expectedContent = Content()

      val expectedResponse = Response.success(
        expectedContent
      )
      // When
      whenever(progressionApi.updatePlaybackProgress(anyString(), any())).doReturn(expectedResponse)

      // Then
      val result = repository.updatePlaybackProgress(
        "RickAndMorty",
        "1",
        10,
        10
      )
      Truth.assertThat(result).isEqualTo(expectedResponse)
      verify(progressionApi).updatePlaybackProgress(
        "1", PlaybackProgress("RickAndMorty", 10, 10)
      )
      verifyNoMoreInteractions(progressionApi)
    }
  }

  @Test
  fun updateWatchStat() {
    testCoroutineRule.runBlockingTest {
      val today = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)

      repository.updateWatchStat("1", 50, today)

      verify(progressionDataSourceLocal).updateWatchStat("1", 50, today)
      verifyNoMoreInteractions(progressionDataSourceLocal)
    }
  }

  @Test
  fun getWatchStats() {
    testCoroutineRule.runBlockingTest {
      repository.getWatchStats()

      verify(progressionDataSourceLocal).getWatchStats()
      verifyNoMoreInteractions(progressionDataSourceLocal)
    }
  }

  @Test
  fun updateWatchStats() {
    testCoroutineRule.runBlockingTest {
      val contents = Contents(datum = listOf(Data(type = "watch_stats")))
      repository.updateWatchStats(contents)

      verify(progressionApi).updateWatchStats(contents)
      verifyNoMoreInteractions(progressionApi)
    }
  }

  @Test
  fun deleteWatchStats() {
    testCoroutineRule.runBlockingTest {
      repository.deleteWatchStats()

      verify(progressionDataSourceLocal).deleteWatchStats()
      verifyNoMoreInteractions(progressionDataSourceLocal)
    }
  }
}
