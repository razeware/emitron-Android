package com.raywenderlich.emitron.data.bookmarks

import com.raywenderlich.emitron.model.Content
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

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


  companion object {

    /**
     * Factory function for [BookmarkApi]
     */
    fun create(retroFit: Retrofit): BookmarkApi = retroFit.create(
      BookmarkApi::class.java
    )
  }
}
