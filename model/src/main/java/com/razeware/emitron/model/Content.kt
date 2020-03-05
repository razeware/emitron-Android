package com.razeware.emitron.model

import android.os.Parcelable
import com.razeware.emitron.model.entity.Progression
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
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
   *  @return percentage completion for content
   */
  fun getPercentComplete(): Int = datum?.getPercentComplete() ?: 0

  /**
   *  @return progress for content
   */
  fun getProgress(): Long = datum?.getProgress() ?: 0

  /**
   *  @return true if user has finished content, else false
   */
  fun isFinished(): Boolean = datum?.isFinished() ?: false

  /**
   * @return true if user has downloaded content, else false
   */
  fun isDownloaded(): Boolean = datum?.isDownloaded() ?: false

  /**
   * @return true if user has cached content, else false
   */
  fun isCached(): Boolean = datum?.isCached() ?: false

  /**
   * Get id for content
   *
   * @return String content id
   */
  fun getChildId(): String? = datum?.id

  /**
   * Get id for content of progression
   *
   * @return String child content id
   */
  private fun getChildContentId(): String = datum?.getContentId() ?: ""

  /**
   * Get video playback token
   *
   */
  fun getPlayerToken(): String? = datum?.getVideoPlaybackToken()

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
  fun getContentGroupIds(): List<String> = datum?.getContentGroupIds() ?: emptyList()

  /**
   *  The following function will return all the episode ids only
   *  if the [Content.getContentType] is [ContentType.Collection]
   */
  fun getEpisodeIds(): List<String> = getContentGroupIds()
    .mapNotNull { id ->
      getIncludedContentById(id)
    }.flatMap {
      it.getChildContentIds()
    }.mapNotNull { id ->
      val includedContent = getIncludedContentById(id)
      includedContent?.id
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
  fun isTypeScreencast(): Boolean = getContentType().isScreencast()

  /**
   * Get if content type is collection
   *
   * @return True if content is [ContentType.Collection], else False
   */
  fun isTypeCollection(): Boolean = getContentType().isCollection()

  /**
   * Get included progressions
   *
   * @return list of included progressions
   */
  fun getIncludedProgressions(): List<Data> =
    included?.filter { it.isTypeProgression() } ?: emptyList()

  /**
   * Get included content by id
   *
   * @return data found by id or null
   */
  fun getIncludedContentById(id: String?): Data? = included?.firstOrNull { it.id == id }

  /**
   * Get included groups
   *
   * @return list of included groups
   */
  fun getIncludedGroups(): List<Data> = included?.filter { it.isTypeGroup() } ?: emptyList()

  /**
   * Get video id
   *
   * @return video id
   */
  fun getVideoId(): String? = getData()?.getVideoId()

  /**
   * Get name
   *
   * @return name of content from [Content.datum]
   */
  fun getName(): String? = getData()?.getName()

  /**
   * Get updatedAt
   *
   * @return updated at of content from [Content.datum]
   */
  private fun getUpdatedAt(): String? = getData()?.getUpdatedAt()

  /**
   * Create [Progression] from [Content]
   *
   */
  fun toProgression(): Progression = Progression(
    contentId = getChildContentId(),
    progressionId = getChildId(),
    percentComplete = getPercentComplete(),
    finished = isFinished(),
    synced = true,
    updatedAt = getUpdatedAt()
  )

  /**
   * Toggle content finished
   */
  fun updateFinished(contentId: String, finished: Boolean): Content = this.copy(
    datum = getData()?.toggleFinished(contentId, finished) ?: Data(
      type = DataType.Progressions.toRequestFormat()
    ).toggleFinished(
      contentId,
      finished
    )
  )

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
  }
}
