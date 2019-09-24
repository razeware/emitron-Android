package com.raywenderlich.emitron.ui.mytutorial.progressions

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.*
import com.raywenderlich.emitron.data.progressions.ProgressionRepository
import com.raywenderlich.emitron.model.Attributes
import com.raywenderlich.emitron.model.Content
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.model.Relationships
import com.raywenderlich.emitron.ui.collection.createContent
import com.raywenderlich.emitron.ui.collection.createContentData
import com.raywenderlich.emitron.utils.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
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

      // Given
      val response = createContent(
        data = createContentData(
          id = "10"
        )
      )
      whenever(progressionRepository.updateProgression("8")).doReturn(response to true)

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
      viewModel.updateContentProgression(episodeData, episodePosition, boundaryCallbackNotifier)

      // Then
      verify(progressionRepository).updateProgression("8")
      verify(progressionRepository).updateProgressionInDb("8", true)
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

      // Given
      val response = createContent(
        data = createContentData(
          id = "10"
        )
      )
      whenever(progressionRepository.updateProgression("8")).doReturn(response to false)

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
      verify(progressionRepository).updateProgression("8")
      verify(progressionRepository).updateProgressionInDb("8", false)
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

      // Given
      whenever(progressionRepository.updateProgression("8")).doThrow(IOException())

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
      verify(progressionRepository).updateProgression("8")
      verify(progressionRepository).updateProgressionInDb("8", false)
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

      // Given
      val response = createContent(
        data = createContentData(
          id = "10"
        )
      )
      whenever(progressionRepository.updateProgression("8")).doReturn(response to true)

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
      verify(progressionRepository).updateProgression("8")
      verify(progressionRepository).updateProgressionInDb("8", false)
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

      // Given
      val response = createContent(
        data = createContentData(
          id = "10"
        )
      )
      whenever(progressionRepository.updateProgression("8")).doReturn(response to false)

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
      verify(progressionRepository).updateProgression("8")
      verify(progressionRepository).updateProgressionInDb("8", true)
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
      whenever(progressionRepository.updateProgression("8")).doThrow(IOException())

      val episodePosition = 4

      // When
      viewModel.completionActionResult.observeForTestingResultNullable()
      viewModel.updateContentProgression(episodeData, episodePosition)

      // Then
      verify(progressionRepository).updateProgression("8")
      verify(progressionRepository).updateProgressionInDb("8", true)
      verifyNoMoreInteractions(progressionRepository)
      viewModel.completionActionResult.value?.first?.peekContent() isEqualTo
          ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeFailedToMarkInProgress
      viewModel.completionActionResult.value?.second isEqualTo
          episodePosition
    }
  }
}
