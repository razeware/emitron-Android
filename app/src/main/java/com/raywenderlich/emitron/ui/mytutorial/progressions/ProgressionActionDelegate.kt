package com.raywenderlich.emitron.ui.mytutorial.progressions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.load.HttpException
import com.raywenderlich.emitron.data.progressions.ProgressionRepository
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.utils.BoundaryCallbackNotifier
import com.raywenderlich.emitron.utils.Event
import com.raywenderlich.emitron.utils.decrement
import com.raywenderlich.emitron.utils.increment
import java.io.IOException
import javax.inject.Inject

/**
 * Delegate class to mark a content in progress/finished
 */
class ProgressionActionDelegate @Inject constructor(
  private val progressionRepository: ProgressionRepository
) {

  /**
   * Progression action API result
   */
  enum class EpisodeProgressionActionResult {
    /**
     * Episode progression complete request succeeded
     */
    EpisodeMarkedCompleted,
    /**
     * Episode progression complete request succeeded
     */
    EpisodeMarkedInProgress,
    /**
     * Episode progression in progress request succeeded
     */
    EpisodeFailedToMarkComplete,
    /**
     * Episode progression in progress request succeeded
     */
    EpisodeFailedToMarkInProgress,
  }

  private val _completionActionResult =
    MutableLiveData<Pair<Event<EpisodeProgressionActionResult>, Int>>()
  /**
   * Observer for bookmark action
   *
   */
  val completionActionResult: LiveData<Pair<Event<EpisodeProgressionActionResult>, Int>>
    get() {
      return _completionActionResult
    }

  /**
   * Mark episode completed/in-progress
   */
  suspend fun updateContentProgression(
    episode: Data?,
    position: Int,
    boundaryCallbackNotifier: BoundaryCallbackNotifier? = null
  ) {
    if (null == episode) {
      return
    }

    boundaryCallbackNotifier.increment()
    if (episode.isFinished()) {
      updateContentInProgress(episode.id, position)
      boundaryCallbackNotifier.decrement()
    } else {
      updateContentCompleted(episode.id, position)
      boundaryCallbackNotifier.decrement()
    }
  }

  private suspend fun updateContentCompleted(episodeId: String?, position: Int) {
    episodeId?.let {
      val contents = try {
        progressionRepository.updateProgression(episodeId, true)
      } catch (exception: IOException) {
        null
      } catch (exception: HttpException) {
        null
      }

      _completionActionResult.value = if (null != contents) {
        progressionRepository.updateProgressionInDb(episodeId, true)
        Event(EpisodeProgressionActionResult.EpisodeMarkedCompleted) to position
      } else {
        progressionRepository.updateProgressionInDb(episodeId, false)
        Event(EpisodeProgressionActionResult.EpisodeFailedToMarkComplete) to position
      }
    }
  }

  private suspend fun updateContentInProgress(episodeId: String?, position: Int) {
    episodeId?.let {
      val contents = try {
        progressionRepository.updateProgression(episodeId, false)
      } catch (exception: IOException) {
        null
      } catch (exception: HttpException) {
        null
      }
      _completionActionResult.value = if (null != contents) {
        progressionRepository.updateProgressionInDb(episodeId, false)
        Event(EpisodeProgressionActionResult.EpisodeMarkedInProgress) to position
      } else {
        progressionRepository.updateProgressionInDb(episodeId, true)
        Event(EpisodeProgressionActionResult.EpisodeFailedToMarkInProgress) to position
      }
    }
  }
}
