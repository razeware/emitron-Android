package com.raywenderlich.emitron.network

interface AuthInterceptor {
  fun updateAuthToken(apiToken: String)
  fun clear()
}
