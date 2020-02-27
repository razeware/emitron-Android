package com.razeware.emitron.data.content

import com.razeware.emitron.model.Content
import com.razeware.emitron.model.Contents
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

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
    @Query("filter[content_types][]") contentType: List<String> = emptyList(),
    @Query("filter[category_ids][]") category: List<String> = emptyList(),
    @Query("filter[domain_ids][]") domain: List<String> = emptyList(),
    @Query("filter[difficulties][]") difficulty: List<String> = emptyList(),
    @Query("filter[q]") search: String = "",
    @Query("sort") sort: String = "",
    @Query("filter[professional]") professional: Boolean? = null
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

  companion object {

    /**
     * Factory function for [ContentApi]
     */
    fun create(retroFit: Retrofit): ContentApi = retroFit.create(
      ContentApi::class.java
    )
  }
}
