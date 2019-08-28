package com.raywenderlich.emitron.utils

/**
 * Enum class to represent status of Network request
 */
enum class NetworkState {
  /**
   * Request in progress
   */
  RUNNING,
  /**
   * Request is successful
   */
  SUCCESS,
  /**
   * Request failed
   */
  FAILED,
  /**
   * Initial request in progress
   */
  INIT,
  /**
   * Initial request failed
   */
  INIT_FAILED,
  /**
   * Initial request successful, but no data
   */
  INIT_EMPTY,
  /**
   * Initial request successful
   */
  INIT_SUCCESS
}

