package com.razeware.emitron.network

interface AuthInterceptor {
  fun updateAuthToken(apiToken: String)
  fun clear()
}
