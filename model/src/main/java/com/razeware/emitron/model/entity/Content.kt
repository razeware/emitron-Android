package com.razeware.emitron.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.razeware.emitron.model.*
import com.razeware.emitron.model.Download


/**
 * Entity to store contents to database
 */
@Entity(
  tableName = Content.TABLE_NAME,
  indices = [Index("content_id", unique = true)]
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
  val streamUrl: String?,

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
  val bookmarkId: String? = null,

  /**
   * Updated at
   */
  @ColumnInfo(name = "updated_at")
  val updatedAt: String
) {

  /**
   * Build [Data] from [Content]
   */
  fun toData(downloadState: Download? = null): Data = Data(
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
      professional = professional
    ),
    download = downloadState
  ).addBookmark(bookmarkId)

  /**
   * Build [Data] from [Content]
   */
  fun toGroupData(): Data = Data(
    id = contentId,
    type = DataType.Contents.toRequestFormat()
  )

  /**
   * Is content screencast or episode
   */
  fun isScreencastOrEpisode(): Boolean = ContentType.fromValue(contentType).run {
    this.isScreencast() || this.isEpisode()
  }

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
        professional = it.isProfessional(),
        deleted = false,
        contentType = it.getContentType()?.toRequestFormat(),
        difficulty = it.getDifficulty()?.toRequestFormat(),
        releasedAt = it.getReleasedAt(),
        technology = it.getTechnology(),
        duration = it.getDuration(),
        streamUrl = "",
        cardArtworkUrl = it.getCardArtworkUrl(),
        videoId = it.getVideoId(),
        bookmarkId = it.getBookmarkId(),
        updatedAt = ""
      )
    }

    /**
     * Create [Content] from [Data]
     */
    fun from(data: Data): Content = Content(
      contentId = data.id!!,
      name = data.getName(),
      description = data.getDescription(),
      contributors = data.getContributors(),
      professional = data.isProfessional(),
      deleted = false,
      contentType = data.getContentType()?.toRequestFormat(),
      difficulty = data.getDifficulty()?.toRequestFormat(),
      releasedAt = data.getReleasedAt(),
      technology = data.getTechnology(),
      duration = data.getDuration(),
      streamUrl = data.getUrl(),
      cardArtworkUrl = data.getCardArtworkUrl(),
      videoId = data.getVideoId(),
      bookmarkId = data.getBookmarkId(),
      updatedAt = ""
    )

    /**
     * List of episodes from content
     */
    fun episodesFrom(
      content: com.razeware.emitron.model.Content
    ): List<Content> =
      content.getContentGroupIds()
        .mapNotNull { id ->
          content.getIncludedContentById(id)
        }.flatMap {
          it.getChildContentIds()
        }.mapNotNull { id ->
          val includedContent = content.getIncludedContentById(id)
          includedContent?.let {
            from(includedContent)
          }
        }
  }
}
