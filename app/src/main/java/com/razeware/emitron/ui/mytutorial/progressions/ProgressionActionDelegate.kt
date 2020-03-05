package com.razeware.emitron.ui.mytutorial.progressions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.load.HttpException
import com.razeware.emitron.data.progressions.ProgressionRepository
import com.razeware.emitron.model.Data
import com.razeware.emitron.utils.BoundaryCallbackNotifier
import com.razeware.emitron.utils.Event
import com.razeware.emitron.utils.decrement
import com.razeware.emitron.utils.increment
import org.threeten.bp.LocalDateTime
import java.io.IOException
import javax.inject.Inject

/**
 * Session permission action
 */
interface ProgressionAction {
  /**
   * LiveData for permission action
   */
  val completionActionResult: LiveData<
      Pair<Event<ProgressionActionDelegate.EpisodeProgressionActionResult>, Int>>

  /**
   * LiveData to enqueue offline progress update
   */
  val enqueueOfflineProgressUpdate: LiveData<String>
}

/**
 * Delegate class to mark a content in progress/finished
 */
class ProgressionActionDelegate @Inject constructor(
  private val progressionRepository: ProgressionRepository
) : ProgressionAction {

  /**
   * Progression action API result
   */
  enum class EpisodeProgressionActionResult {
    /**
     * Episode progression complete request succeeded
     */
    EpisodeMarkedCompleted,
    /**
     * Episode progression in progress request succeeded
     */
    EpisodeMarkedInProgress,
    /**
     * Episode progression complete request failed
     */
    EpisodeFailedToMarkComplete,
    /**
     * Episode progression in progress request failed
     */
    EpisodeFailedToMarkInProgress
  }

  private val _completionActionResult =
    MutableLiveData<Pair<Event<EpisodeProgressionActionResult>, Int>>()

  private val _enqueueOfflineProgressUpdate = MutableLiveData<String>()

  /**
   * Observer for bookmark action
   *
   */
  override val completionActionResult: LiveData<Pair<Event<EpisodeProgressionActionResult>, Int>>
    get() {
      return _completionActionResult
    }

  /**
   * Observer for offline progress update
   */
  override val enqueueOfflineProgressUpdate: LiveData<String>
    get() = _enqueueOfflineProgressUpdate

  /**
   * Mark episode completed/in-progress
   */
  suspend fun updateContentProgression(
    hasConnection: Boolean,
    episode: Data?,
    position: Int,
    boundaryCallbackNotifier: BoundaryCallbackNotifier? = null,
    updatedAt: LocalDateTime
  ) {
    episode ?: return
    val id = episode.id ?: return
    val isCompleted = episode.isProgressionFinished()
    val progress = episode.getProgress()
    val percentComplete = episode.getPercentComplete()
    val progressionId = episode.getProgressionId()

    if (hasConnection) {
      updateContentProgressionToServer(
        id,
        percentComplete,
        progress,
        isCompleted,
        position,
        boundaryCallbackNotifier,
        updatedAt,
        progressionId
      )
    } else {
      updateContentProgressionLocal(
        id,
        percentComplete,
        progress,
        isCompleted,
        position,
        updatedAt,
        progressionId
      )
    }
  }

  private suspend fun updateContentProgressionToServer(
    id: String,
    percentComplete: Int,
    progress: Long,
    isCompleted: Boolean,
    position: Int,
    boundaryCallbackNotifier: BoundaryCallbackNotifier? = null,
    updatedAt: LocalDateTime,
    progressionId: String?
  ) {
    boundaryCallbackNotifier.increment()
    if (isCompleted) {
      updateContentInProgress(
        id,
        percentComplete,
        progress,
        position,
        updatedAt,
        progressionId
      )
      boundaryCallbackNotifier.decrement()
    } else {
      updateContentCompleted(
        id,
        percentComplete,
        progress,
        position,
        updatedAt,
        progressionId
      )
      boundaryCallbackNotifier.decrement()
    }
  }

  private suspend fun updateContentProgressionLocal(
    id: String,
    percentComplete: Int,
    progress: Long,
    isCompleted: Boolean,
    position: Int,
    updatedAt: LocalDateTime,
    progressionId: String?
  ) {

    progressionRepository.updateLocalProgression(
      id,
      percentComplete,
      progress,
      finished = false,
      synced = false,
      updatedAt = updatedAt,
      progressionId = progressionId
    )

    _completionActionResult.value = if (isCompleted) {
      Event(EpisodeProgressionActionResult.EpisodeMarkedInProgress) to position
    } else {
      Event(EpisodeProgressionActionResult.EpisodeMarkedCompleted) to position
    }
    _enqueueOfflineProgressUpdate.value = id
  }

  private suspend fun updateContentCompleted(
    id: String,
    percentComplete: Int,
    progress: Long,
    position: Int,
    updatedAt: LocalDateTime,
    progressionId: String?
  ) {
    val contents = try {
      progressionRepository.updateLocalProgression(
        contentId = id,
        percentComplete = percentComplete,
        progress = progress,
        finished = true,
        synced = true,
        updatedAt = updatedAt,
        progressionId = progressionId
      )
      progressionRepository.updateProgressions(id, true, updatedAt)
    } catch (exception: IOException) {
      null
    } catch (exception: HttpException) {
      null
    }

    _completionActionResult.value = if (null != contents) {
      progressionRepository.updateLocalProgression(
        contentId = id,
        percentComplete = percentComplete,
        progress = progress,
        finished = true,
        synced = true,
        updatedAt = updatedAt,
        progressionId = progressionId
      )
      Event(EpisodeProgressionActionResult.EpisodeMarkedCompleted) to position
    } else {
      progressionRepository.updateLocalProgression(
        contentId = id,
        percentComplete = percentComplete,
        progress = progress,
        finished = false,
        synced = true,
        updatedAt = updatedAt,
        progressionId = progressionId
      )
      Event(EpisodeProgressionActionResult.EpisodeFailedToMarkComplete) to position
    }
  }

  private suspend fun updateContentInProgress(
    id: String,
    percentComplete: Int,
    progress: Long,
    position: Int,
    updatedAt: LocalDateTime,
    progressionId: String?
  ) {
    val contents = try {
      progressionRepository.updateLocalProgression(
        id,
        percentComplete,
        progress,
        finished = false,
        synced = true,
        updatedAt = updatedAt,
        progressionId = progressionId
      )
      progressionRepository.updateProgressions(id, false, updatedAt)
    } catch (exception: IOException) {
      null
    } catch (exception: HttpException) {
      null
    }
    _completionActionResult.value = if (null != contents) {
      progressionRepository.updateLocalProgression(
        id,
        percentComplete,
        progress,
        finished = false,
        synced = true,
        updatedAt = updatedAt,
        progressionId = progressionId
      )
      Event(EpisodeProgressionActionResult.EpisodeMarkedInProgress) to position
    } else {
      progressionRepository.updateLocalProgression(
        id,
        percentComplete,
        progress,
        finished = true,
        synced = true,
        updatedAt = updatedAt,
        progressionId = progressionId
      )
      Event(EpisodeProgressionActionResult.EpisodeFailedToMarkInProgress) to position
    }
  }
}
