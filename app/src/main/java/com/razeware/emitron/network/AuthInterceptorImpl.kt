package com.razeware.emitron.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptorImpl @Inject constructor(private var requestHelper: RequestHelper) :
  AuthInterceptor, Interceptor {

  override fun updateAuthToken(apiToken: String) {
    requestHelper = requestHelper.copy(apiAuthToken = apiToken)
  }

  override fun clear() {
    requestHelper = requestHelper.copy(apiAuthToken = "")
  }

  override fun intercept(chain: Interceptor.Chain): Response {
    val requestBuilder = chain.request().newBuilder()
    requestHelper.getHeaders().forEach { (key, value) ->
      requestBuilder.addHeader(key, value)
    }
    return chain.proceed(requestBuilder.build())
  }
}
