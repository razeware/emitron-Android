package com.raywenderlich.emitron.data.content

import com.raywenderlich.emitron.model.Content
import com.raywenderlich.emitron.model.Contents
import com.raywenderlich.emitron.model.PlaybackProgress
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.*

/**
 * Service to make API calls for contents
 */
interface ContentApi {

  /**
   * Fetch all contents
   *
   * Default content type is set to screencast, collection
   */
  @GET("contents")
  fun getContents(
    @Query("page[number]") pageNumber: Int,
    @Query("page[size]") pageSize: Int,
    @Query("filter[content_types][]") contentType: List<String> = listOf(
      "collection",
      "screencast"
    ),
    @Query("filter[category_ids][]") category: List<String> = emptyList(),
    @Query("filter[domain_ids][]") domain: List<String> = emptyList(),
    @Query("filter[q]") search: String = "",
    @Query("sort") sort: String = ""
  ): Call<Contents>

  /**
   * Fetch a content by id
   *
   * @param id Id for content
   *
   * @return [Content]
   */
  @GET("contents/{id}")
  @Throws(Exception::class)
  suspend fun getContent(
    @Path("id") id: String
  ): Content

  /**
   * Get playback token for user
   *
   * @return [Content]
   */
  @POST("contents/begin_playback")
  @Throws(Exception::class)
  suspend fun getPlaybackToken(
  ): Content

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
     * Factory function for [ContentApi]
     */
    fun create(retroFit: Retrofit): ContentApi = retroFit.create(
      ContentApi::class.java
    )
  }
}
