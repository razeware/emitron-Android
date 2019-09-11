package com.raywenderlich.emitron.model.entity

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import com.raywenderlich.emitron.model.Attributes
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.model.DataType


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

  @PrimaryKey
  @ColumnInfo(name = "content_id")
  val contentId: String,

  val description: String?,

  val contributors: String?,

  val free: Boolean = false,

  val deleted: Boolean,

  val name: String?,

  @ColumnInfo(name = "content_type")
  val contentType: String?,

  @ColumnInfo(name = "difficulty")
  val difficulty: String?,

  @ColumnInfo(name = "released_at")
  val releasedAt: String,

  @ColumnInfo(name = "download_progress")
  val downloadProgress: Int,

  val technology: String?,

  val duration: Long,

  @ColumnInfo(name = "stream_url")
  val streamUrl: String,

  @ColumnInfo(name = "card_artwork_url")
  val cardArtworkUrl: String?,

  @ColumnInfo(name = "video_id")
  val videoId: String?,

  @ColumnInfo(name = "bookmark_id")
  val bookmarkId: String?,

  @ColumnInfo(name = "progression_id")
  val progressionId: String?,

  @ColumnInfo(name = "updated_at")
  val updatedAt: String
) {

  fun toData(): Data = Data(
    id = contentId,
    type = DataType.Contents.toRequestFormat(),
    attributes = Attributes(
      name = name,
      description = description,
      contributors = contributors,
      free = free,
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
        free = it.isFreeContent(),
        deleted = false,
        contentType = it.getContentType()?.toRequestFormat(),
        difficulty = it.getDifficulty()?.toRequestFormat(),
        releasedAt = it.getReleasedAt(),
        downloadProgress = 0,
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
