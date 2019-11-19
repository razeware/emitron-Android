package com.razeware.emitron.model

/**
 * DTO for download progress
 */
data class DownloadProgress(
  /**
   * Content id
   */
  val contentId: String,
  /**
   * Percent downloaded
   */
  val percentDownloaded: Int,
  /**
   * State [DownloadState]
   */
  val state: DownloadState
)
