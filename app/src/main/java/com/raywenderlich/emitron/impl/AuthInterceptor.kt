package com.raywenderlich.emitron.impl

interface AuthInterceptor {
  fun updateApiToken(apiToken: String)
  fun clear()
}
