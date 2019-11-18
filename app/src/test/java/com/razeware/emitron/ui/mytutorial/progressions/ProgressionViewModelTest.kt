package com.razeware.emitron.ui.mytutorial.progressions

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.*
import com.razeware.emitron.data.progressions.ProgressionRepository
import com.razeware.emitron.model.*
import com.razeware.emitron.ui.content.ContentPagedViewModel
import com.razeware.emitron.utils.*
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Month

class ProgressionViewModelTest {

  private val progressionRepository: ProgressionRepository = mock()

  private val contentViewModel: ContentPagedViewModel = ContentPagedViewModel()

  private val progressionActionDelegate: ProgressionActionDelegate = mock()

  private val boundaryCallbackNotifier: BoundaryCallbackNotifier = BoundaryCallbackNotifier()

  private lateinit var viewModel: ProgressionViewModel

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  private fun createViewModel() {
    viewModel = ProgressionViewModel(
      progressionRepository,
      contentViewModel,
      progressionActionDelegate,
      boundaryCallbackNotifier
    )
  }

  @Test
  fun init() {
    whenever(
      progressionActionDelegate.completionActionResult
    ).doReturn(
      MutableLiveData<Pair<Event<ProgressionActionDelegate.EpisodeProgressionActionResult>, Int>>().apply {
        value =
          Event(ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeMarkedCompleted) to 1
      }
    )
    createViewModel()
    viewModel.completionActionResult.observeForTestingResultNullable()

    verify(progressionActionDelegate).completionActionResult
    viewModel.completionActionResult.value?.first?.peekContent() isEqualTo
        ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeMarkedCompleted
  }

  @Test
  fun loadBookmarks() {
    createViewModel()

    val response: LocalPagedResponse<Data> = mock()
    whenever(
      progressionRepository.getProgressions(
        CompletionStatus.Completed,
        boundaryCallbackNotifier
      )
    ).doReturn(response)

    // When
    viewModel.loadProgressions(CompletionStatus.Completed)

    // Then
    verify(progressionRepository).getProgressions(
      CompletionStatus.Completed,
      boundaryCallbackNotifier
    )
    verifyNoMoreInteractions(progressionRepository)
  }

  @Test
  fun getPaginationViewModel() {
    createViewModel()
    viewModel.getPaginationViewModel() isEqualTo contentViewModel
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
      verify(progressionActionDelegate)
        .updateContentProgression(true, episodeData, episodePosition, boundaryCallbackNotifier, day)
      verifyNoMoreInteractions(progressionActionDelegate)

      with(viewModel) {
        completionActionResult.value?.first?.peekContent() isEqualTo
            ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeMarkedCompleted
        completionActionResult.value?.second isEqualTo
            episodePosition
      }
      boundaryCallbackNotifier.hasRequests() isEqualTo false
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
      viewModel.updateContentProgression(true, episodeData, episodePosition, day)

      // Then
      verify(progressionActionDelegate).completionActionResult
      verify(progressionActionDelegate)
        .updateContentProgression(true, episodeData, episodePosition, boundaryCallbackNotifier, day)
      verifyNoMoreInteractions(progressionActionDelegate)
      with(viewModel) {
        completionActionResult.value?.first?.peekContent() isEqualTo
            ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeFailedToMarkComplete
        completionActionResult.value?.second isEqualTo
            episodePosition
      }
      boundaryCallbackNotifier.hasRequests() isEqualTo false
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
      verify(progressionActionDelegate)
        .updateContentProgression(true, episodeData, episodePosition, boundaryCallbackNotifier, day)
      verifyNoMoreInteractions(progressionActionDelegate)
      with(viewModel) {
        completionActionResult.value?.first?.peekContent() isEqualTo
            ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeMarkedInProgress
        completionActionResult.value?.second isEqualTo
            episodePosition
      }
      boundaryCallbackNotifier.hasRequests() isEqualTo false
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
      verify(progressionActionDelegate)
        .updateContentProgression(true, episodeData, episodePosition, boundaryCallbackNotifier, day)
      verifyNoMoreInteractions(progressionActionDelegate)
      with(viewModel) {
        completionActionResult.value?.first?.peekContent() isEqualTo
            ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeFailedToMarkInProgress
        completionActionResult.value?.second isEqualTo
            episodePosition
      }
      boundaryCallbackNotifier.hasRequests() isEqualTo false
    }
  }
}
