package com.razeware.emitron.network

import androidx.collection.ArrayMap
import com.razeware.emitron.BuildConfig

data class RequestHelper @JvmOverloads constructor(
  private val appToken: String = "",
  private val apiAuthToken: String = ""
) {

  fun getHeaders(): ArrayMap<String, String> {
    val headers = ArrayMap<String, String>()
    headers[SOURCE] =
      ANDROID
    headers[HEADER_CLIENT_NAME] = BuildConfig.APPLICATION_ID
    headers[HEADER_CLIENT_VERSION] = BuildConfig.VERSION_NAME
    headers[ACCEPT] = "application/vnd.api+json; charset=utf-8"
    headers[APP_TOKEN] = appToken

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
    private const val APP_TOKEN = "RW-App-Token"
  }
}
