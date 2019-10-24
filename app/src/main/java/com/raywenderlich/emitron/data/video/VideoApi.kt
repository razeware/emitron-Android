package com.raywenderlich.emitron.data.video

import com.raywenderlich.emitron.model.Content
import com.raywenderlich.emitron.model.Contents
import com.raywenderlich.emitron.model.PlaybackProgress
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Service to make API calls for videos
 */
interface VideoApi {

  /**
   * Get video stream data
   */
  @GET("videos/{id}/stream")
  suspend fun getVideoStream(
    @Path("id") id: String
  ): Content

  /**
   * Get download url
   */
  @GET("videos/{id}/download")
  fun getDownloadUrl(
    @Path("id") id: String
  ): Call<Contents>

  /**
   * Get playback token for user
   *
   * @return [Content]
   */
  @POST("contents/begin_playback")
  @Throws(Exception::class)
  suspend fun getPlaybackToken(
  ): Content?

  /**
   * Get playback token for user
   *
   * @return [Content]
   */
  @POST("contents/{id}/playback")
  @Throws(Exception::class)
  suspend fun updateContentPlayback(
    @Path("id") id: String,
    @Body data: PlaybackProgress
  ): Response<Content>

  companion object {

    /**
     * Factory function for [VideoApi]
     */
    fun create(retroFit: Retrofit): VideoApi = retroFit.create(
      VideoApi::class.java
    )
  }
}
