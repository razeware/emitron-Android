package com.razeware.emitron.data.video

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.*
import com.razeware.emitron.model.Content
import com.razeware.emitron.utils.CurrentThreadExecutor
import com.razeware.emitron.utils.TestCoroutineRule
import com.razeware.emitron.utils.async.ThreadManager
import kotlinx.coroutines.Dispatchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString

class VideoRepositoryTest {

  private lateinit var repository: VideoRepository

  private val videoApi: VideoApi = mock()

  private val threadManager: ThreadManager = mock()

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  @Before
  fun setUp() {
    whenever(threadManager.io).doReturn(Dispatchers.Unconfined)
    whenever(threadManager.networkExecutor).doReturn(CurrentThreadExecutor())
    repository = VideoRepository(videoApi, threadManager)
  }

  @Test
  fun getVideoStream() {
    testCoroutineRule.runBlockingTest {
      // Given
      val expectedContent = Content()

      // When
      whenever(videoApi.getVideoStream("1")).doReturn(expectedContent)

      // Then
      val result = repository.getVideoStream("1")
      Truth.assertThat(result).isEqualTo(expectedContent)

      verify(videoApi).getVideoStream("1")
      verifyNoMoreInteractions(videoApi)
    }
  }

  @Test(expected = Exception::class)
  fun getVideoStream_failure() {
    testCoroutineRule.runBlockingTest {
      // Given
      val expected = RuntimeException()
      whenever(videoApi.getVideoStream(anyString())).doThrow(expected)

      // When
      repository.getVideoStream("1")

      // Then
      verify(videoApi).getVideoStream("1")
      verifyNoMoreInteractions(videoApi)
    }
  }

  @Test
  fun getVideoPlaybackToken() {
    testCoroutineRule.runBlockingTest {
      // Given
      val expectedContent = Content()

      // When
      whenever(videoApi.getPlaybackToken()).doReturn(expectedContent)

      // Then
      val result = repository.getVideoPlaybackToken()
      Truth.assertThat(result).isEqualTo(expectedContent)

      verify(videoApi).getPlaybackToken()
      verifyNoMoreInteractions(videoApi)
    }
  }

  @Test(expected = Exception::class)
  fun getVideoPlaybackToken_failure() {
    testCoroutineRule.runBlockingTest {
      // Given
      val expected = RuntimeException()
      whenever(videoApi.getPlaybackToken()).doThrow(expected)

      // When
      repository.getVideoPlaybackToken()

      // Then
      verify(videoApi).getPlaybackToken()
      verifyNoMoreInteractions(videoApi)
    }
  }
}
