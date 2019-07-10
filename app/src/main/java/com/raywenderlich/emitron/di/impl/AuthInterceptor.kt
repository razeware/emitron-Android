package com.raywenderlich.emitron.di.impl

interface AuthInterceptor {
  fun updateApiToken(apiToken: String)
  fun clear()
}
