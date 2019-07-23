package com.raywenderlich.emitron.network

interface AuthInterceptor {
  fun updateApiToken(apiToken: String)
  fun clear()
}
