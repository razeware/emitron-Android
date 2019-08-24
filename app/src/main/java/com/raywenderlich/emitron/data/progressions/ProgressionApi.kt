package com.raywenderlich.emitron.data.progressions

import com.raywenderlich.emitron.model.Content
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Service for API calls for Progressions
 */
interface ProgressionApi {

  /**
   * Create/Update a progression
   *
   * @param data to create progression
   *
   * @return Response<Content> response object containing response body
   */
  @POST("progressions")
  suspend fun createProgression(
    @Body data: Content
  ): Response<Content>

  /**
   * Delete a progression
   *
   * @param id progression id
   *
   * @return Response<Any> response object containing response body
   */
  @DELETE("progressions/{id}")
  suspend fun deleteProgression(
    @Path("id") id: String
  ): Response<Any>


  companion object {

    /**
     * Factory function for [ProgressionApi]
     */
    fun create(retroFit: Retrofit): ProgressionApi = retroFit.create(
      ProgressionApi::class.java
    )
  }
}
