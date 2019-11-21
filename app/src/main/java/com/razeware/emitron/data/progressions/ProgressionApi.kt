package com.razeware.emitron.data.progressions

import com.razeware.emitron.model.Content
import com.razeware.emitron.model.Contents
import com.razeware.emitron.model.PlaybackProgress
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.*

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
  @POST("progressions/bulk")
  @Throws(Exception::class)
  suspend fun updateProgression(
    @Body data: Contents
  ): Contents?

  /**
   * Delete a progression
   *
   * @param id progression id
   *
   * @return Response<Any> response object containing response body
   */
  @DELETE("progressions/{id}")
  @Throws(Exception::class)
  suspend fun deleteProgression(
    @Path("id") id: String
  ): Response<Any>

  /**
   * Fetch all progressions
   */
  @GET("progressions")
  fun getProgressions(
    @Query("page[number]") pageNumber: Int,
    @Query("page[size]") pageSize: Int,
    @Query("filter[content_types][]") contentType: List<String> = listOf(
      "screencast",
      "collection"
    ),
    @Query("filter[completion_status]") completionStatus: String
  ): Call<Contents>

  /**
   * Get playback token for user
   *
   * @return [Content]
   */
  @POST("contents/{id}/playback")
  @Throws(Exception::class)
  suspend fun updatePlaybackProgress(
    @Path("id") id: String,
    @Body data: PlaybackProgress
  ): Response<Content>

  companion object {

    /**
     * Factory function for [ProgressionApi]
     */
    fun create(retroFit: Retrofit): ProgressionApi = retroFit.create(
      ProgressionApi::class.java
    )
  }

  /**
   * Update watch stats
   */
  @POST("watch_stats/bulk")
  @Throws(Exception::class)
  suspend fun updateWatchStats(
    @Body data: Contents
  ): Response<Contents?>
}
