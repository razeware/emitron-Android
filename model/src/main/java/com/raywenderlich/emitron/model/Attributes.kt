package com.raywenderlich.emitron.model

import android.os.Parcelable
import com.raywenderlich.emitron.model.utils.TimeUtils
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDateTime

/**
 * Model class for content attributes
 */
@Parcelize
data class Attributes(

  /**
   * Created at
   */
  @Json(name = "created_at")
  val createdAt: String? = "",

  /**
   * Readable description
   */
  val description: String? = "",

  /**
   * Level [DomainLevel]
   */
  val level: String? = "",

  /**
   * Readable name
   */
  val name: String? = "",

  /**
   * Slug
   */
  val slug: String? = "",

  /**
   * Is content bookmarked?
   */
  @Json(name = "bookmarked?")
  val bookmarked: Boolean? = false,

  /**
   * Card artwork url
   */
  @Json(name = "card_artwork_url")
  val cardArtworkUrl: String? = "",

  /**
   * Type of content [ContentType]
   */
  @Json(name = "content_type")
  val contentType: String? = "",

  /**
   * Content difficulty [Difficulty]
   */
  val difficulty: String? = "",

  /**
   * Content duration
   */
  val duration: Long? = 0,

  /**
   * Is content free?
   */
  val free: Boolean? = false,

  /**
   * Content popularity
   */
  val popularity: Double? = 0.0,

  /**
   * Content released at date
   */
  @Json(name = "released_at")
  val releasedAt: String? = "",

  /**
   * Content url
   */
  val uri: String? = "",

  /**
   * Content target
   */
  val target: Long? = 0,

  /**
   * Content current progress
   */
  val progress: Long? = 0,

  /**
   * User has finished content
   */
  val finished: Boolean? = false,

  /**
   * Content completion percentage
   */
  @Json(name = "percent_complete")
  val percentComplete: Double? = 0.0,

  /**
   * Content updated at
   */
  @Json(name = "updated_at")
  val updatedAt: String? = "",

  /**
   * Content technology
   */
  @Json(name = "technology_triple_string")
  val technology: String? = "",

  /**
   * Content authors
   */
  @Json(name = "contributor_string")
  val contributors: String? = "",

  /**
   * Content url
   */
  val url: String? = "",

  /**
   * Content kind
   */
  val kind: String? = ""
) : Parcelable {

  fun getReadableReleasedAt(
    withYear: Boolean,
    today: LocalDateTime
  ): TimeUtils.Day =
    if (releasedAt.isNullOrBlank()) {
      TimeUtils.Day.None
    } else {
      TimeUtils.toReadableDate(releasedAt, withYear, today = today)
    }

  fun getDurationHoursAndMinutes(): Pair<Long, Long> = if (null == duration) {
    0L to 0L
  } else {
    TimeUtils.toHoursAndMinutes(duration)
  }

  fun getContentType(): ContentType? = ContentType.fromValue(contentType)

  fun getDifficulty(): Difficulty? = Difficulty.fromValue(difficulty)

  fun isLevelArchived(): Boolean = DomainLevel.Archived == DomainLevel.fromValue(level)

  fun getPercentComplete(): Int = percentComplete?.toInt() ?: 0
}
