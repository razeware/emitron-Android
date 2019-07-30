package com.raywenderlich.emitron.data.login

import com.raywenderlich.emitron.model.Content
import retrofit2.Retrofit
import retrofit2.http.GET

interface LoginApi {

  /**
   * Request a subscription
   */
  @GET("subscription")
  suspend fun getSubscription(): Content

  companion object {

    /**
     * Factory method for [LoginApi]
     */
    fun create(retroFit: Retrofit): LoginApi = retroFit.create(
      LoginApi::class.java
    )
  }
}