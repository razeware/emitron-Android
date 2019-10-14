package com.raywenderlich.emitron.model

import com.squareup.moshi.Json
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

data class ProgressionUpdate(
  @Json(name = "content_id")
  val contentId: Int,
  val progress: Int = 0,
  val finished: Boolean,
  @Json(name = "updated_at")
  val updatedAt: String
)

data class ProgressionsUpdate(
  val progressions: List<ProgressionUpdate>
) {

  companion object {

    /**
     * Create content object for creating new progression
     *
     * @param contentId id of content for which progression has to be created/updated
     * @param finished true if content is completed else false
     * @param updatedAt Update at time
     */
    fun newProgressionsUpdate(
      contentId: String,
      finished: Boolean,
      updatedAt: LocalDateTime
    ): ProgressionsUpdate {
      return ProgressionsUpdate(
        progressions = listOf(
          ProgressionUpdate(
            contentId.toInt(), finished = finished,
            updatedAt = updatedAt.format(DateTimeFormatter.ISO_DATE_TIME)
          )
        )
      )
    }
  }
}
