package com.raywenderlich.emitron.data.progressions

import com.raywenderlich.emitron.data.content.dao.ContentDao
import com.raywenderlich.emitron.data.progressions.dao.ProgressionDao
import com.raywenderlich.emitron.model.entity.Progression
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Local data source to update progression
 */
class ProgressionDataSourceLocal @Inject constructor(
  private val progressionDao: ProgressionDao,
  private val contentDao: ContentDao
) {

  /**
   * Update content progress
   *
   * @param contentId Content id
   * @param percentComplete Percentage completion
   * @param progress Content progress
   * @param finished Has content finished
   * @param synced Has content synced? False if content was updated offline, else True
   * @param updatedAt Content updated time
   * @param progressionId Progression Id
   */
  suspend fun updateProgress(
    contentId: String,
    percentComplete: Int,
    progress: Long,
    finished: Boolean,
    synced: Boolean,
    updatedAt: LocalDateTime,
    progressionId: String?
  ) {
    progressionDao.insertOrUpdateProgress(
      contentId,
      percentComplete,
      progress,
      finished,
      synced,
      updatedAt.format(DateTimeFormatter.ISO_DATE_TIME),
      progressionId,
      contentDao
    )
  }

  /**
   * Get local updated progressions
   *
   * @return List of progressions updated offline/local
   */
  suspend fun getLocalProgressions(): List<Progression> {
    return progressionDao.getLocalProgressions()
  }

  /**
   * Update progressions
   *
   * @param progressions List of progressions
   */
  suspend fun updateLocalProgressions(progressions: List<Progression>) {
    return progressionDao.updateProgressionsAsync(progressions)
  }
}
