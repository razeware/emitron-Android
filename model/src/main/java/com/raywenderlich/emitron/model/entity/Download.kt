package com.raywenderlich.emitron.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.raywenderlich.emitron.model.DownloadState

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
   * @return [com.raywenderlich.emitron.model.Download] from [Download]
   */
  fun toDownloadState(): com.raywenderlich.emitron.model.Download? {
    return com.raywenderlich.emitron.model.Download(
      progress = this.progress,
      state = this.state,
      url = this.url,
      failureReason = failureReason
    )
  }

  companion object {

    /**
     * Table name to store domains
     */
    const val TABLE_NAME: String = "downloads"
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
