package com.raywenderlich.emitron.model.entity

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import com.raywenderlich.emitron.model.Attributes
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.model.DataType
import com.raywenderlich.emitron.model.toRequestFormat


/**
 * Entity to store contents to database
 */
@Entity(
  tableName = Content.TABLE_NAME,
  indices = [Index("content_id", unique = true), Index("progression_id")],
  foreignKeys = [
    ForeignKey(
      entity = Progression::class,
      parentColumns = ["progression_id"],
      childColumns = ["progression_id"],
      onUpdate = CASCADE,
      onDelete = CASCADE
    )
  ]
)
data class Content(

  /**
   * Content id
   */
  @PrimaryKey
  @ColumnInfo(name = "content_id")
  val contentId: String,

  /**
   * Description
   */
  val description: String?,

  /**
   * Contributors
   */
  val contributors: String?,

  /**
   * Professional
   */
  val professional: Boolean = false,

  /**
   * Deleted
   */
  val deleted: Boolean,

  /**
   * Name
   */
  val name: String?,

  /**
   * Content type
   */
  @ColumnInfo(name = "content_type")
  val contentType: String?,

  /**
   * Difficulty
   */
  @ColumnInfo(name = "difficulty")
  val difficulty: String?,

  /**
   * Released At
   */
  @ColumnInfo(name = "released_at")
  val releasedAt: String,

  /**
   * Technology
   */
  val technology: String?,

  /**
   * Duration
   */
  val duration: Long,

  /**
   * Stream url
   */
  @ColumnInfo(name = "stream_url")
  val streamUrl: String,

  /**
   * Card artwork url
   */
  @ColumnInfo(name = "card_artwork_url")
  val cardArtworkUrl: String?,

  /**
   * Video Id
   */
  @ColumnInfo(name = "video_id")
  val videoId: String?,

  /**
   * Bookmark Id
   */
  @ColumnInfo(name = "bookmark_id")
  val bookmarkId: String?,

  /**
   * Progression Id
   */
  @ColumnInfo(name = "progression_id")
  val progressionId: String?,

  /**
   * Updated at
   */
  @ColumnInfo(name = "updated_at")
  val updatedAt: String
) {

  /**
   * Build [Data] from [Content]
   */
  fun toData(): Data = Data(
    id = contentId,
    type = DataType.Contents.toRequestFormat(),
    attributes = Attributes(
      name = name,
      description = description,
      contributors = contributors,
      free = professional,
      duration = duration,
      contentType = contentType,
      difficulty = difficulty,
      releasedAt = releasedAt,
      technology = technology,
      cardArtworkUrl = cardArtworkUrl,
      url = streamUrl
    )
  ).addBookmark(bookmarkId)

  companion object {

    /**
     * Table name to store categories
     */
    const val TABLE_NAME: String = "contents"

    /**
     * Create list of [Category] from list of [Data]
     *
     * @return list of [Category]
     */
    fun listFrom(contents: List<Data>): List<Content> = contents.map {
      Content(
        contentId = it.id!!,
        name = it.getName(),
        description = it.getDescription(),
        contributors = it.getContributors(),
        professional = it.isFreeContent(),
        deleted = false,
        contentType = it.getContentType()?.toRequestFormat(),
        difficulty = it.getDifficulty()?.toRequestFormat(),
        releasedAt = it.getReleasedAt(),
        technology = it.getTechnology(),
        duration = it.getDuration(),
        streamUrl = "",
        cardArtworkUrl = it.getCardArtworkUrl(),
        videoId = "",
        bookmarkId = it.getBookmarkId(),
        progressionId = it.getProgressionId(),
        updatedAt = ""
      )
    }
  }
}
