package com.razeware.emitron.data.filter

import com.razeware.emitron.model.Contents
import retrofit2.Retrofit
import retrofit2.http.GET

/**
 * Service to make API calls for filters
 */
interface FilterApi {

  /**
   * Fetch all categories
   *
   * @return [Contents] where [Contents.datum] is list of categories
   */
  @GET("categories")
  suspend fun getCategories(): Contents

  /**
   * Fetch all domains
   *
   * @return [Contents] where [Contents.datum] is list of domains
   */
  @GET("domains")
  suspend fun getDomains(): Contents

  companion object {

    /**
     * Factory function for [FilterApi]
     */
    fun create(retroFit: Retrofit): FilterApi = retroFit.create(
      FilterApi::class.java
    )
  }
}
