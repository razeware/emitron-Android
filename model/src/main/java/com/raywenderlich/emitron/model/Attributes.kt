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
  val createdAt: String? = null,

  /**
   * Readable description
   */
  @Json(name = "description_plain_text")
  val description: String? = null,

  /**
   * Level [DomainLevel]
   */
  val level: String? = null,

  /**
   * Readable name
   */
  val name: String? = null,

  /**
   * Slug
   */
  val slug: String? = null,

  /**
   * Card artwork url
   */
  @Json(name = "card_artwork_url")
  val cardArtworkUrl: String? = null,

  /**
   * Type of content [ContentType]
   */
  @Json(name = "content_type")
  val contentType: String? = null,

  /**
   * Content difficulty [Difficulty]
   */
  val difficulty: String? = null,

  /**
   * Content duration
   */
  val duration: Long? = 0,

  /**
   * Is content free?
   */
  val free: Boolean? = false,

  /**
   * Is content free?
   */
  val professional: Boolean? = false,

  /**
   * Content popularity
   */
  val popularity: Double? = 0.0,

  /**
   * Content released at date
   */
  @Json(name = "released_at")
  val releasedAt: String? = null,

  /**
   * Content url
   */
  val uri: String? = null,

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
  val updatedAt: String? = null,

  /**
   * Content technology
   */
  @Json(name = "technology_triple_string")
  val technology: String? = null,

  /**
   * Content authors
   */
  @Json(name = "contributor_string")
  val contributors: String? = null,

  /**
   * Content url
   */
  val url: String? = null,

  /**
   * Content kind
   */
  val kind: String? = null,

  /**
   * Video playback token
   */
  @Json(name = "video_playback_token")
  val videoPlaybackToken: String? = null,

  /**
   * Content position
   */
  val ordinal: Int? = 0,

  /**
   * Tag
   */
  val tag: String? = null,

  /**
   * Tag
   */
  @Json(name = "video_identifier")
  val videoId: String? = null
) : Parcelable {

  /**
   * Get releasedAt formatted to [TimeUtils.Day]
   *
   * @return [TimeUtils.Day]
   */
  fun getReadableReleasedAt(
    withYear: Boolean,
    today: LocalDateTime
  ): TimeUtils.Day =
    if (releasedAt.isNullOrBlank()) {
      TimeUtils.Day.None
    } else {
      TimeUtils.toReadableDate(releasedAt, withYear, today = today)
    }

  /**
   * Get Pair of hours, minutes
   */
  fun getDurationHoursAndMinutes(): Pair<Long, Long> = if (null == duration) {
    0L to 0L
  } else {
    TimeUtils.toHoursAndMinutes(duration)
  }

  /**
   * Get Triple of hours, minutes, seconds
   */
  fun getDurationHoursAndMinutesAndSeconds(): Triple<Long, Long, Long> = if (null == duration) {
    Triple(0L, 0L, 0L)
  } else {
    TimeUtils.toHoursAndMinutesAndSeconds(duration)
  }

  /**
   * Get content type of data
   *
   * @return [ContentType]
   */
  fun getContentType(): ContentType? = ContentType.fromValue(contentType)

  /**
   * Get content difficulty
   *
   * @return [Difficulty]
   */
  fun getDifficulty(): Difficulty? = Difficulty.fromValue(difficulty)

  /**
   * Check if Domain level is achieved
   *
   * @return True if domain is archived else False
   */
  fun isLevelArchived(): Boolean = DomainLevel.Archived == DomainLevel.fromValue(level)

  /**
   * Get percent completion for the content
   *
   * @return [Int] percent completion value
   */
  fun getPercentComplete(): Int = percentComplete?.toInt() ?: 0

  /**
   * Get current progress for content
   *
   * @return progress
   */
  fun getProgress(): Long = progress ?: 0

  /**
   * Set video url
   *
   * @param attributes attribute with video url
   */
  fun setVideoUrl(attributes: Attributes?): Attributes = this.copy(url = attributes?.url)
}
