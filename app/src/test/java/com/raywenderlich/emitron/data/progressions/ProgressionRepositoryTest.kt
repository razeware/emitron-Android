package com.raywenderlich.emitron.data.progressions

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.*
import com.raywenderlich.emitron.data.content.ContentDataSourceLocal
import com.raywenderlich.emitron.model.Contents
import com.raywenderlich.emitron.model.ProgressionUpdate
import com.raywenderlich.emitron.model.ProgressionsUpdate
import com.raywenderlich.emitron.utils.TestCoroutineRule
import com.raywenderlich.emitron.utils.async.ThreadManager
import kotlinx.coroutines.Dispatchers
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Month
import org.threeten.bp.format.DateTimeFormatter
import retrofit2.Response

class ProgressionRepositoryTest {

  private lateinit var repository: ProgressionRepository

  private val progressionApi: ProgressionApi = mock()

  private val threadManager: ThreadManager = mock()

  private val contentDataSourceLocal: ContentDataSourceLocal = mock()

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  @Before
  fun setUp() {
    whenever(threadManager.io).doReturn(Dispatchers.Unconfined)
    repository = ProgressionRepository(progressionApi, threadManager, contentDataSourceLocal)
  }

  @Test
  fun updateProgression() {
    testCoroutineRule.runBlockingTest {
      val day = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)
      val updatedAt = day.format(DateTimeFormatter.ISO_DATE_TIME)

      val expectedContent = Contents()
      whenever(progressionApi.updateProgression(any())).doReturn(expectedContent)
      val result = repository.updateProgression(
        "1",
        true,
        day
      )
      verify(progressionApi).updateProgression(
        ProgressionsUpdate(
          listOf(ProgressionUpdate(1, finished = true, updatedAt = updatedAt))
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
      val updatedAt = day.format(DateTimeFormatter.ISO_DATE_TIME)

      whenever(progressionApi.updateProgression(any())).doReturn(null)

      val result =
        repository.updateProgression("1", true, day)

      verify(progressionApi).updateProgression(
        ProgressionsUpdate(
          listOf(ProgressionUpdate(1, finished = true, updatedAt = updatedAt))
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
  fun updateProgressionInDb() {
    testCoroutineRule.runBlockingTest {
      repository.updateProgressionInDb("1", true)

      verify(contentDataSourceLocal).updateProgress("1", true)
      verifyNoMoreInteractions(contentDataSourceLocal)
    }
  }
}
