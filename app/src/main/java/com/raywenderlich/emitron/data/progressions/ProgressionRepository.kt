package com.raywenderlich.emitron.data.progressions

import com.raywenderlich.emitron.model.Content
import com.raywenderlich.emitron.utils.async.ThreadManager
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Repository for progression operations
 */
class ProgressionRepository @Inject constructor(
  private val api: ProgressionApi,
  private val threadManager: ThreadManager
) {

  /**
   * Create/Update a progression
   *
   * @param contentId Content id for progression to be created/updated
   *
   * @return Pair of response [Content] and True/False if request was succeeded/failed
   */
  suspend fun createProgression(contentId: String): Pair<Content?, Boolean> {
    val progression = Content.newProgression(contentId)
    return withContext(threadManager.io) {
      val response = api.createProgression(progression)
      response.body() to response.isSuccessful
    }
  }

  /**
   * Delete a progression
   *
   * @param progressionId progression id to be deleted
   *
   * @return True, if request was successful, else False
   */
  suspend fun deleteProgression(progressionId: String): Boolean {
    return withContext(threadManager.io) {
      val response = api.deleteProgression(progressionId)
      response.isSuccessful
    }
  }
}
