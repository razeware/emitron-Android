package com.razeware.emitron.data.login

import com.razeware.emitron.model.Contents
import retrofit2.Retrofit
import retrofit2.http.GET

/**
 * Service to make API calls respective to sessions
 */
interface LoginApi {

  /**
   * Request a subscription
   */
  @GET("permissions")
  suspend fun getPermissions(): Contents

  companion object {

    /**
     * Factory function for [LoginApi]
     */
    fun create(retroFit: Retrofit): LoginApi = retroFit.create(
      LoginApi::class.java
    )
  }
}
