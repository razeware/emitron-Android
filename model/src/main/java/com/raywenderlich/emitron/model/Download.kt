package com.raywenderlich.emitron.model

import android.os.Parcelable
import com.raywenderlich.emitron.model.entity.inProgress
import com.raywenderlich.emitron.model.entity.isCompleted
import kotlinx.android.parcel.Parcelize

/**
 * Download VO
 */
@Parcelize
data class Download(
  /**
   * Progress
   */
  val progress: Int = 0,
  /**
   * State [DownloadState]
   */
  val state: Int = DownloadState.COMPLETED.ordinal,
  /**
   * Failure reason [DownloadFailureReason]
   */
  val failureReason: Int = DownloadFailureReason.NONE.ordinal,

  /**
   * Download url
   */
  val url: String? = null
) : Parcelable {

  companion object {

    /**
     * Get collection download state
     *
     * @param downloads Downloads for each episode
     */
    fun fromEpisodeDownloads(downloads: List<com.raywenderlich.emitron.model.entity.Download>)
        : Download? {
      return if (downloads.isNotEmpty()) {
        val downloadProgress: Pair<Int, Int> = when {
          downloads.any { it.inProgress() } -> {
            (downloads.map {
              it.progress
            }.reduce { acc, i ->
              (i + acc)
            }.toFloat() / downloads.size).toInt() to DownloadState.IN_PROGRESS.ordinal
          }
          downloads.all { it.isCompleted() } -> {
            100 to DownloadState.COMPLETED.ordinal
          }
          else -> {
            0 to DownloadState.CREATED.ordinal
          }
        }

        Download(
          progress = downloadProgress.first,
          state = downloadProgress.second
        )
      } else {
        null
      }
    }
  }
}

/**
 * @return true if download has completed, else false
 */
fun Download?.isDownloaded(): Boolean {
  return this?.state == DownloadState.COMPLETED.ordinal ||
      this?.progress == 100
}

/**
 * @return true if download is downloading, else false
 */
fun Download?.isDownloading(): Boolean {
  return this?.state == DownloadState.IN_PROGRESS.ordinal
}

/**
 * @return true if download is pending, else false
 */
fun Download?.isPending(): Boolean {
  return this?.state == DownloadState.CREATED.ordinal
}

/**
 * @return true if download has failed, else false
 */
fun Download?.isFailed(): Boolean {
  return this?.state == DownloadState.FAILED.ordinal
}

/**
 * @return true if download has failed, else false
 */
fun Download?.isPaused(): Boolean {
  return this?.state == DownloadState.PAUSED.ordinal
}

/**
 * @return download progress or 0
 */
fun Download?.getProgress(): Int = this?.progress ?: 0
