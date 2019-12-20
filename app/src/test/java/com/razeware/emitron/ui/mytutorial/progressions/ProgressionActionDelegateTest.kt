package com.razeware.emitron.ui.mytutorial.progressions

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.*
import com.razeware.emitron.data.createContentData
import com.razeware.emitron.data.progressions.ProgressionRepository
import com.razeware.emitron.model.*
import com.razeware.emitron.utils.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Month
import java.io.IOException

class ProgressionActionDelegateTest {

  private lateinit var viewModel: ProgressionActionDelegate

  private val progressionRepository: ProgressionRepository = mock()

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  @Before
  fun setUp() {
    viewModel = ProgressionActionDelegate(progressionRepository)
  }

  @Test
  fun updateContentProgression_markCompletedSuccess() {
    testCoroutineRule.runBlockingTest {
      val day = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)

      // Given
      val response = Contents(
        datum = listOf(
          createContentData(
            id = "10"
          )
        )
      )
      whenever(progressionRepository.updateProgressions("8", true, day))
        .doReturn(response)

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

      val boundaryCallbackNotifier = BoundaryCallbackNotifier()

      // When
      viewModel.completionActionResult.observeForTestingResultNullable()
      viewModel.updateContentProgression(
        true,
        episodeData,
        episodePosition,
        boundaryCallbackNotifier,
        updatedAt = day
      )

      // Then
      verify(progressionRepository).updateProgressions("8", true, day)
      verify(progressionRepository, times(2)).updateLocalProgression(
        contentId = "8",
        percentComplete = 0,
        progress = 0,
        finished = true,
        synced = true,
        updatedAt = day,
        progressionId = "10"
      )
      verifyNoMoreInteractions(progressionRepository)

      boundaryCallbackNotifier.hasRequests() isEqualTo false
      viewModel.completionActionResult.value?.first?.peekContent() isEqualTo
          ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeMarkedCompleted
      viewModel.completionActionResult.value?.second isEqualTo
          episodePosition
    }
  }

  @Test
  fun updateContentProgression_markCompletedFailure() {
    testCoroutineRule.runBlockingTest {

      val day = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)

      // Given
      whenever(progressionRepository.updateProgressions("8", true, day))
        .doReturn(null)

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
      verify(progressionRepository).updateLocalProgression(
        contentId = "8",
        percentComplete = 0,
        progress = 0,
        finished = true,
        synced = true,
        updatedAt = day,
        progressionId = "10"
      )
      verify(progressionRepository).updateProgressions("8", true, day)
      verify(progressionRepository).updateLocalProgression(
        contentId = "8",
        percentComplete = 0,
        progress = 0,
        finished = false,
        synced = true,
        updatedAt = day,
        progressionId = "10"
      )
      verifyNoMoreInteractions(progressionRepository)

      viewModel.completionActionResult.value?.first?.peekContent() isEqualTo
          ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeFailedToMarkComplete
      viewModel.completionActionResult.value?.second isEqualTo
          episodePosition
    }
  }

  @Test
  fun toggleEpisodeCompleted_markCompletedApiError() {
    testCoroutineRule.runBlockingTest {
      val day = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)

      // Given
      whenever(progressionRepository.updateProgressions("8", true, day))
        .doThrow(IOException())

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
      verify(progressionRepository).updateProgressions("8", true, day)
      verify(progressionRepository).updateLocalProgression(
        contentId = "8",
        percentComplete = 0,
        progress = 0L,
        finished = true,
        synced = true,
        updatedAt = day,
        progressionId = "10"
      )
      verify(progressionRepository).updateLocalProgression(
        contentId = "8",
        percentComplete = 0,
        progress = 0L,
        finished = false,
        synced = true,
        updatedAt = day,
        progressionId = "10"
      )
      verifyNoMoreInteractions(progressionRepository)

      viewModel.completionActionResult.value?.first?.peekContent() isEqualTo
          ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeFailedToMarkComplete
      viewModel.completionActionResult.value?.second isEqualTo
          episodePosition
    }
  }

  @Test
  fun toggleEpisodeCompleted_markInProgressSuccess() {
    testCoroutineRule.runBlockingTest {
      val day = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)

      // Given
      val response = Contents(
        datum = listOf(
          createContentData(
            id = "10"
          )
        )
      )
      whenever(progressionRepository.updateProgressions("8", false, day))
        .doReturn(response)

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
      viewModel.updateContentProgression(
        true,
        episodeData,
        episodePosition,
        updatedAt = day
      )

      // Then
      verify(progressionRepository).updateProgressions("8", false, day)
      verify(progressionRepository, times(2)).updateLocalProgression(
        contentId = "8",
        percentComplete = 0,
        progress = 0L,
        finished = false,
        synced = true,
        updatedAt = day,
        progressionId = "10"
      )
      verifyNoMoreInteractions(progressionRepository)

      viewModel.completionActionResult.value?.first?.peekContent() isEqualTo
          ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeMarkedInProgress
      viewModel.completionActionResult.value?.second isEqualTo
          episodePosition
    }
  }

  @Test
  fun updateContentProgression_markInProgressFailure() {
    testCoroutineRule.runBlockingTest {
      val day = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)

      // Given
      whenever(progressionRepository.updateProgressions("8", false, day))
        .doReturn(null)

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
      viewModel.updateContentProgression(true, episodeData, episodePosition, updatedAt = day)

      // Then
      verify(progressionRepository).updateProgressions("8", false, day)
      verify(progressionRepository).updateLocalProgression(
        contentId = "8",
        percentComplete = 0,
        progress = 0,
        finished = false,
        synced = true,
        updatedAt = day,
        progressionId = "10"
      )
      verify(progressionRepository).updateLocalProgression(
        contentId = "8",
        percentComplete = 0,
        progress = 0,
        finished = true,
        synced = true,
        updatedAt = day,
        progressionId = "10"
      )
      verifyNoMoreInteractions(progressionRepository)

      viewModel.completionActionResult.value?.first?.peekContent() isEqualTo
          ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeFailedToMarkInProgress
      viewModel.completionActionResult.value?.second isEqualTo
          episodePosition
    }
  }

  @Test
  fun updateContentProgression_markInProgressApiError() {
    testCoroutineRule.runBlockingTest {

      // Given
      val day =
        LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)

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
      whenever(progressionRepository.updateProgressions("8", false, day))
        .doThrow(IOException())

      val episodePosition = 4

      // When
      viewModel.completionActionResult.observeForTestingResultNullable()
      viewModel.updateContentProgression(true, episodeData, episodePosition, updatedAt = day)

      // Then
      verify(progressionRepository).updateProgressions("8", false, day)
      verify(progressionRepository).updateLocalProgression(
        contentId = "8",
        percentComplete = 0,
        progress = 0,
        finished = false,
        synced = true,
        updatedAt = day,
        progressionId = "10"
      )
      verify(progressionRepository).updateLocalProgression(
        contentId = "8",
        percentComplete = 0,
        progress = 0,
        finished = true,
        synced = true,
        updatedAt = day,
        progressionId = "10"
      )
      verifyNoMoreInteractions(progressionRepository)
      viewModel.completionActionResult.value?.first?.peekContent() isEqualTo
          ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeFailedToMarkInProgress
      viewModel.completionActionResult.value?.second isEqualTo
          episodePosition
    }
  }

  @Test
  fun updateContentProgression_offlineProgress() {
    testCoroutineRule.runBlockingTest {

      // Given
      val day =
        LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)

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
      whenever(progressionRepository.updateProgressions("8", false, day))
        .doThrow(IOException())

      val episodePosition = 4

      // When
      viewModel.completionActionResult.observeForTestingResultNullable()
      viewModel.enqueueOfflineProgressUpdate.observeForTestingResultNullable()
      viewModel.updateContentProgression(false, episodeData, episodePosition, updatedAt = day)

      // Then
      verify(progressionRepository).updateLocalProgression(
        contentId = "8",
        percentComplete = 0,
        progress = 0,
        finished = false,
        synced = false,
        updatedAt = day,
        progressionId = "10"
      )
      verifyNoMoreInteractions(progressionRepository)
      viewModel.completionActionResult.value?.first?.peekContent() isEqualTo
          ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeMarkedInProgress
      viewModel.completionActionResult.value?.second isEqualTo episodePosition
      viewModel.enqueueOfflineProgressUpdate.value isEqualTo "8"
    }
  }
}
