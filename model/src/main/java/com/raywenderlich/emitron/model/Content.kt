package com.raywenderlich.emitron.model

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

/**
 * Model for Bookmark, Content response
 */
@JsonClass(generateAdapter = true)
@Parcelize
data class Content(
  /**
   * Included data
   */
  @Json(name = "data")
  var datum: Data? = null,
  /**
   * Links
   */
  val links: Links? = null,
  /**
   * Included Meta data
   */
  val meta: Meta? = null,
  /**
   * Included meta data
   */
  val included: List<Data>? = null
) : Parcelable {

  /**
   * User has subscription
   */
  @IgnoredOnParcel
  var hasSubscription: Boolean = false

  /**
   *  @return percentage completion for content
   */
  fun getPercentComplete(): Int? = datum?.getPercentComplete() ?: 0

  /**
   *  @return true if user has finished content, else false
   */
  fun isFinished(): Boolean = datum?.isFinished() ?: false

  /**
   * Get id for content
   *
   * @return String content id
   */
  fun getChildId(): String? = datum?.id

  /**
   * Get content data, along with included metadata.
   *
   * @return content
   */
  fun getData(): Data? {
    if (included.isNullOrEmpty()) {
      return datum
    }

    return datum?.updateRelationships(included)
  }

  /**
   * Get included groups
   */
  fun getGroups(): List<Data> {
    return included?.filter { it.isTypeGroup() } ?: emptyList()
  }

  /**
   * Get type of Content
   *
   * @return [ContentType]
   */
  private fun getContentType(): ContentType? = datum?.getContentType()

  /**
   * Get if content type is screencast
   *
   * @return True if content is [ContentType.Screencast], else False
   */
  fun isTypeScreencast(): Boolean = getContentType()?.isScreenCast() ?: false

  companion object {

    /**
     * Create content object for creating new bookmark
     *
     * @param contentId id of content for which bookmark is to be created
     */
    fun newBookmark(contentId: String): Content {
      return Content(
        datum = Data(
          type = DataType.Bookmarks.toRequestFormat(),
          relationships = Relationships(
            content =
            Content(
              datum = Data(
                type = DataType.Contents.toRequestFormat(),
                id = contentId
              )
            )
          )
        )
      )
    }

    /**
     * Create content object for creating new progression
     *
     * @param contentId id of content for which progression has to be created/updated
     */
    fun newProgression(contentId: String): Content {
      return Content(
        datum = Data(
          type = DataType.Progressions.toRequestFormat(),
          relationships = Relationships(
            content =
            Content(
              datum = Data(
                type = DataType.Contents.toRequestFormat(),
                id = contentId
              )
            )
          )
        )
      )
    }
  }
}
