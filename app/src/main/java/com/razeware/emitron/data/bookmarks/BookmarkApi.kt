package com.razeware.emitron.data.bookmarks

import com.razeware.emitron.model.Content
import com.razeware.emitron.model.Contents
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.*

/**
 * Service to make API calls for Bookmarks
 */
interface BookmarkApi {

  /**
   * Create bookmark
   *
   * @param data to create bookmark
   *
   * @return Response<Content> response object containing response body
   */
  @POST("bookmarks")
  @Throws(Exception::class)
  suspend fun createBookmark(
    @Body data: Content
  ): Response<Content>

  /**
   * Delete bookmark
   *
   * @param id id of bookmark for deletion
   *
   * @return Response<Any> response object containing response body
   */
  @DELETE("bookmarks/{id}")
  @Throws(Exception::class)
  suspend fun deleteBookmark(
    @Path("id") id: String
  ): Response<Any>

  /**
   * Fetch all bookmarks
   */
  @GET("bookmarks")
  @Throws(Exception::class)
  fun getBookmarks(
    @Query("page[number]") pageNumber: Int,
    @Query("page[size]") pageSize: Int,
    @Query("filter[content_types][]") contentType: List<String> = listOf(
      "screencast",
      "collection"
    )
  ): Call<Contents>

  companion object {

    /**
     * Factory function for [BookmarkApi]
     */
    fun create(retroFit: Retrofit): BookmarkApi = retroFit.create(
      BookmarkApi::class.java
    )
  }
}
