package com.razeware.emitron.model

/**
 * Download state
 */
enum class DownloadState {

  /**
   * None
   */
  NONE,
  /**
   * Created
   */
  CREATED,
  /**
   * Downloading
   */
  IN_PROGRESS,
  /**
   * Completed
   */
  COMPLETED,
  /**
   * Failed
   */
  FAILED,
  /**
   * Paused
   */
  PAUSED
}

/**
 * Download failure reason
 */
enum class DownloadFailureReason {
  /**
   * None
   */
  NONE,
  /**
   * No access
   */
  NO_ACCESS
}
