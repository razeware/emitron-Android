package com.raywenderlich.emitron.utils

/**
 * Class to represent network request state
 */
data class NetworkState(
  /**
   * Status of current request
   */
  val status: Status,
  /**
   * Any success of failure message
   */
  val msg: String? = null
) {

  /**
   * Enum class to represent status of Network request
   */
  enum class Status {
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
    FAILED
  }

  companion object {
    /**
     * Network request has completed
     */
    val LOADED: NetworkState = NetworkState(Status.SUCCESS)

    /**
     * Network request is in progress
     */
    val LOADING: NetworkState = NetworkState(Status.RUNNING)

    /**
     * Network request failed
     */
    val ERROR: NetworkState = NetworkState(Status.FAILED)

    /**
     * [INIT], [INIT_EMPTY], [INIT_ERROR] should be used with paginated APIs
     *
     * First page network request in progress
     */
    val INIT: NetworkState = NetworkState(Status.RUNNING, "Init running")

    /**
     * First page network request failed
     */
    val INIT_ERROR: NetworkState = NetworkState(Status.FAILED, "Init failed")

    /**
     * First page network request succeed, but no data received
     */
    val INIT_EMPTY: NetworkState = NetworkState(Status.FAILED, "Init no data")

    /**
     * Factory function to create [NetworkState.ERROR] with an error message
     */
    fun error(msg: String?): NetworkState = NetworkState(Status.FAILED, msg)
  }
}

