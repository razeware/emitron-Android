package com.raywenderlich.emitron.utils

import androidx.collection.ArrayMap
import com.raywenderlich.emitron.BuildConfig

data class RequestHelper @JvmOverloads constructor(
  private val apiAuthToken: String = ""
) {

  fun getHeaders(): ArrayMap<String, String> {
    val headers = ArrayMap<String, String>()
    headers[SOURCE] =
      ANDROID
    headers[HEADER_CLIENT_NAME] = BuildConfig.APPLICATION_ID
    headers[HEADER_CLIENT_VERSION] = BuildConfig.VERSION_NAME
    headers[ACCEPT] = "application/vnd.api+json; charset=utf-8"

    if (apiAuthToken.isNotEmpty()) {
      headers[AUTH] = "$BEARER$apiAuthToken"
    }

    return headers
  }

  companion object {
    private const val ACCEPT = "Accept"
    private const val AUTH = "Authorization"
    private const val BEARER = "Bearer "
    private const val HEADER_CLIENT_NAME = "client-name"
    private const val HEADER_CLIENT_VERSION = "client-version"
    private const val SOURCE = "source"
    private const val ANDROID = "android"
  }
}
