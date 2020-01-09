package com.razeware.emitron.model

import android.os.Parcelable
import com.razeware.emitron.model.entity.inProgress
import com.razeware.emitron.model.entity.isCompleted
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

/**
 * Download VO
 */
@JsonClass(generateAdapter = true)
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
  val url: String? = null,

  /**
   * Meta of download is cached
   */
  val cached: Boolean = false
) : Parcelable {

  companion object {

    /**
     * Get collection download state
     *
     * @param downloads Downloads for each episode
     */
    fun fromEpisodeDownloads(
      downloads: List<com.razeware.emitron.model.entity.Download>,
      downloadIds: List<String>
    ): Download? {

      return if (downloads.isNotEmpty()) {
        val downloadProgress: Pair<Int, Int> = when {
          downloads.any { it.inProgress() } -> {
            (downloads.map {
              it.progress
            }.reduce { acc, i ->
              (i + acc)
            }.toFloat() / downloads.size).toInt() to DownloadState.PAUSED.ordinal
          }
          downloadIds.size == downloads.size && downloads.all { it.isCompleted() } -> {
            100 to DownloadState.COMPLETED.ordinal
          }
          else -> {
            0 to DownloadState.PAUSED.ordinal
          }
        }

        val cached = downloads.any { it.inProgress() || it.isCompleted() }

        Download(
          progress = downloadProgress.first,
          state = downloadProgress.second,
          cached = cached
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
 * @return true if download has cached meta data
 */
fun Download?.isCached(): Boolean {
  return this?.cached ?: false
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

/**
 * @return download state or [DownloadState.PAUSED]
 */
fun Download?.getState(): Int = this?.state ?: DownloadState.PAUSED.ordinal
