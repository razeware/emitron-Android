package com.razeware.emitron.ui.mytutorial.progressions

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.razeware.emitron.data.progressions.ProgressionRepository
import com.razeware.emitron.model.CompletionStatus
import com.razeware.emitron.model.Data
import com.razeware.emitron.ui.content.ContentPagedViewModel
import com.razeware.emitron.utils.BoundaryCallbackNotifier
import kotlinx.coroutines.launch
import org.threeten.bp.Clock
import org.threeten.bp.LocalDateTime

/**
 * ViewModel for content in progress/completed
 */
class ProgressionViewModel @ViewModelInject constructor(
  private val repository: ProgressionRepository,
  private val contentPagedViewModel: ContentPagedViewModel,
  private val progressionActionDelegate: ProgressionActionDelegate,
  private val boundaryCallbackNotifier: BoundaryCallbackNotifier
) : ViewModel(), ProgressionAction by progressionActionDelegate {

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
   *
   * @param isConnected Device has internet?
   * @param content Content for which progression to be updated
   * @param position Content position in list
   * @param updatedAt [LocalDateTime] Update time (Default to current time)
   */
  fun updateContentProgression(
    isConnected: Boolean,
    content: Data?,
    position: Int = 0,
    updatedAt: LocalDateTime = LocalDateTime.now(Clock.systemUTC())
  ) {
    viewModelScope.launch {
      progressionActionDelegate.updateContentProgression(
        isConnected,
        content,
        position,
        boundaryCallbackNotifier,
        updatedAt
      )
    }
  }
}
