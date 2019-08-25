package com.raywenderlich.emitron.data.progressions

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.*
import com.raywenderlich.emitron.model.Content
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.model.DataType
import com.raywenderlich.emitron.model.Relationships
import com.raywenderlich.emitron.utils.TestCoroutineRule
import com.raywenderlich.emitron.utils.async.ThreadManager
import kotlinx.coroutines.Dispatchers
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import retrofit2.Response

class ProgressionRepositoryTest {

  private lateinit var repository: ProgressionRepository

  private val progressionApi: ProgressionApi = mock()

  private val threadManager: ThreadManager = mock()

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  @Before
  fun setUp() {
    whenever(threadManager.io).doReturn(Dispatchers.Unconfined)
    repository = ProgressionRepository(progressionApi, threadManager)
  }

  @Test
  fun updateProgression() {
    testCoroutineRule.runBlockingTest {
      val expectedContent = Content()
      val progression = createProgressionStub()
      whenever(progressionApi.createProgression(any())).doReturn(
        Response.success(
          200,
          expectedContent
        )
      )
      val (result, isSuccessful) = repository.updateProgression("1")
      verify(progressionApi).createProgression(progression)
      Truth.assertThat(result).isEqualTo(expectedContent)
      Truth.assertThat(isSuccessful).isTrue()
      verifyNoMoreInteractions(progressionApi)
    }
  }

  @Test
  fun updateProgression_failure() {
    testCoroutineRule.runBlockingTest {
      val expectedContent = createProgressionStub()
      val responseBody: ResponseBody = mock()
      val errorResponse: Response<Content> = Response.error(401, responseBody)
      whenever(progressionApi.createProgression(any())).doReturn(errorResponse)

      val (_, result) = repository.updateProgression("1")

      verify(progressionApi).createProgression(expectedContent)
      Truth.assertThat(result).isFalse()
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

  private fun createProgressionStub() = Content(
    datum = Data(
      type = DataType.Progressions.toRequestFormat(),
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
