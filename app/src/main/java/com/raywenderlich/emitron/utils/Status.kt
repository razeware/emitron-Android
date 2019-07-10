package com.raywenderlich.emitron.utils

enum class Status {
  RUNNING,
  SUCCESS,
  FAILED
}

data class NetworkState(
  val status: Status,
  val msg: String? = null
) {

  companion object {
    val LOADED = NetworkState(Status.SUCCESS)
    val LOADING = NetworkState(Status.RUNNING)
    val ERROR = NetworkState(Status.FAILED)
    val INIT = NetworkState(Status.RUNNING, "Init running")
    val INIT_ERROR = NetworkState(Status.FAILED, "Init failed")
    fun error(msg: String?) = NetworkState(Status.FAILED, msg)
  }
}

sealed class NetworkResponse {
  data class Success<T>(val response: T) : NetworkResponse()
  data class Error<T>(val error: T?) : NetworkResponse()

  fun isSuccessful() = this is Success<*>
}
