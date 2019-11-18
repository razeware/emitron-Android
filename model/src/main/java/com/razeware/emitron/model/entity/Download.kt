package com.razeware.emitron.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.razeware.emitron.model.DownloadState
import org.threeten.bp.Clock
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

/**
 * Entity to store downloads to database
 */
@Entity(
  tableName = Download.TABLE_NAME
)
data class Download(

  /**
   * Download Id
   */
  @PrimaryKey
  @ColumnInfo(name = "download_id")
  val downloadId: String,

  /**
   * Url
   */
  @ColumnInfo(name = "url")
  val url: String? = null,

  /**
   * Progress
   */
  @ColumnInfo(name = "progress")
  val progress: Int = 0,

  /**
   * State
   */
  @ColumnInfo(name = "state")
  val state: Int = 0,

  /**
   * Failure reason
   */
  @ColumnInfo(name = "failure_reason")
  val failureReason: Int = 0,

  /**
   * Created at
   */
  @ColumnInfo(name = "created_at")
  val createdAt: String? = null
) {

  /**
   * @return [com.razeware.emitron.model.Download] from [Download]
   */
  fun toDownloadState(): com.razeware.emitron.model.Download? =
    com.razeware.emitron.model.Download(
      progress = this.progress,
      state = this.state,
      url = this.url,
      failureReason = failureReason
    )

  companion object {

    /**
     * Table name to store domains
     */
    const val TABLE_NAME: String = "downloads"

    /**
     * Create [Download] instance with id
     *
     * @param contentId
     */
    fun with(
      contentId: String,
      createdAt: LocalDateTime = LocalDateTime.now(Clock.systemUTC())
    ): Download = Download(
      contentId,
      state = DownloadState.CREATED.ordinal,
      createdAt = createdAt.format(DateTimeFormatter.ISO_DATE_TIME)
    )
  }
}

/**
 * Download is completed
 */
fun Download?.isCompleted(): Boolean = this?.progress == 100 ||
    this?.state == DownloadState.COMPLETED.ordinal

/**
 * Download is in progress
 */
fun Download?.inProgress(): Boolean = this?.progress != 100
    && this?.state == DownloadState.IN_PROGRESS.ordinal

/**
 * Download is paused
 */
fun Download?.isPaused(): Boolean = this?.state == DownloadState.PAUSED.ordinal
