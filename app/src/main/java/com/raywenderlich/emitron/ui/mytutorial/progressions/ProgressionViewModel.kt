package com.raywenderlich.emitron.ui.mytutorial.progressions

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raywenderlich.emitron.data.progressions.ProgressionRepository
import com.raywenderlich.emitron.model.CompletionStatus
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.ui.content.ContentPagedViewModel
import com.raywenderlich.emitron.utils.BoundaryCallbackNotifier
import com.raywenderlich.emitron.utils.Event
import kotlinx.coroutines.launch
import org.threeten.bp.Clock
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

/**
 * ViewModel for content in progress/completed
 */
class ProgressionViewModel @Inject constructor(
  private val repository: ProgressionRepository,
  private val contentPagedViewModel: ContentPagedViewModel,
  private val progressionActionDelegate: ProgressionActionDelegate,
  private val boundaryCallbackNotifier: BoundaryCallbackNotifier
) : ViewModel() {


  /**
   * Observer for progressions completion action
   *
   */
  val completionActionResult:
      LiveData<Pair<Event<ProgressionActionDelegate.EpisodeProgressionActionResult>, Int>>
    get() {
      return progressionActionDelegate.completionActionResult
    }

  /**
   * Load progressions from database
   */
  fun loadProgressions(completionStatus: CompletionStatus = CompletionStatus.Completed) {
    val listing =
      repository.getProgressions(
        completionStatus = completionStatus,
        boundaryCallbackNotifier = boundaryCallbackNotifier
      )
    contentPagedViewModel.localRepoResult.value = listing
  }

  /**
   * Get pagination helper
   */
  fun getPaginationViewModel(): ContentPagedViewModel = contentPagedViewModel

  /**
   * Update content progression state
   */
  fun updateContentProgression(
    episode: Data?, position: Int = 0,
    updatedAt: LocalDateTime = LocalDateTime.now(Clock.systemUTC())
  ) {
    viewModelScope.launch {
      progressionActionDelegate.updateContentProgression(
        episode,
        position,
        boundaryCallbackNotifier,
        updatedAt
      )
    }
  }
}
