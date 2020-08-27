package com.razeware.emitron.model

import android.os.Parcelable
import com.razeware.emitron.model.entity.Category
import com.razeware.emitron.model.entity.Domain
import com.razeware.emitron.model.entity.Progression
import com.razeware.emitron.model.utils.TimeUtils
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

/**
 *  Model class for Bookmark, Domain, Progression, Content, Group.
 */
@JsonClass(generateAdapter = true)
@Parcelize
data class Data(
  /**
   *  Id
   */
  val id: String? = null,
  /**
   * Maps to [DataType]
   */
  val type: String? = null,
  /**
   *  Attributes
   */
  val attributes: Attributes? = null,
  /**
   *  Links
   */
  val links: Links? = null,
  /**
   *  Relationships
   */
  val relationships: Relationships? = null,
  /**
   *  Meta
   */
  val meta: Meta? = null,
  /**
   *  Contents
   */
  val included: Contents? = null,
  /**
   * Download
   */
  val download: Download? = null
) : Parcelable {
  /**
   *  Name
   *
   *  @return content name ex. Swift UI: Working With UIKit
   */
  fun getName(): String? = attributes?.name

  /**
   * Level
   *
   * @return content level
   */
  fun getLevel(): String? = attributes?.level

  /**
   * Ordinal
   *
   * @return content position
   */
  fun getOrdinal(): Int = attributes?.ordinal ?: 0

  /**
   *  @return true if type is [DataType.Progressions], else false
   */
  fun isTypeProgression(): Boolean = DataType.fromValue(this.type).isProgression()

  /**
   *  @return true if type is [DataType.Contents], else false
   */
  fun isTypeContent(): Boolean = DataType.fromValue(this.type).isContent()

  /**
   *  @return true if type is [DataType.Domains], else false
   */
  fun isTypeDomain(): Boolean = DataType.fromValue(type).isDomain()

  /**
   *  @return true if type is [DataType.Bookmarks], else false
   */
  fun isTypeBookmark(): Boolean = DataType.fromValue(this.type).isBookmark()

  /**
   *  @return content description
   */
  fun getDescription(): String? = attributes?.description

  /**
   *  @return card artwork url for content
   */
  fun getCardArtworkUrl(): String? = attributes?.cardArtworkUrl

  /**
   *  @return true if content requires a professional subscription, else false
   */
  fun isProfessional(): Boolean = attributes?.professional == true

  /**
   *  If data represents a progression object
   *
   *  @return percent completion value for progression
   */
  fun getPercentComplete(): Int = attributes?.getPercentComplete() ?: 0

  /**
   * Get current progress for data
   */
  fun getProgress(): Long = attributes?.getProgress() ?: 0

  /**
   *  @return [ContentType] of content
   */
  fun getContentType(): ContentType? = attributes?.getContentType()

  /**
   *  @return [Difficulty] of content
   */
  fun getDifficulty(): Difficulty? = attributes?.getDifficulty()

  /**
   *  @return true if user has watched the content, else false
   */
  fun isFinished(): Boolean = attributes?.finished ?: false

  /**
   *  @return [TimeUtils.Day] after parsing release date of content
   */
  fun getReleasedAt(
    withYear: Boolean,
    today: LocalDateTime
  ): TimeUtils.Day =
    attributes?.getReadableReleasedAt(withYear, today) ?: TimeUtils.Day.None

  /**
   * @return released at in ISO format
   */
  fun getReleasedAt(): String = attributes?.releasedAt ?: ""

  /**
   *  @return [Pair] of hours, minutes for watch duration content
   */
  fun getDurationHoursAndMinutes(): Pair<Long, Long> =
    attributes?.getDurationHoursAndMinutes() ?: 0L to 0L

  /**
   * @return Content duration in millis
   */
  fun getDuration(): Long = attributes?.duration ?: 0

  /**
   *  @return true if content is bookmarked, else false
   */
  fun isBookmarked(): Boolean = !relationships?.getBookmarkId().isNullOrEmpty()

  /**
   *  @return technology string for content
   */
  fun getTechnology(): String? = attributes?.technology

  /**
   *  @return contributor string for content
   */
  fun getContributors(): String? = attributes?.contributors

  /**
   *  @return true if category is archived
   */
  fun isLevelArchived(): Boolean = attributes?.isLevelArchived() ?: false

  /**
   *  @return true if progression
   */
  fun isProgressionFinished(): Boolean = relationships?.hasFinishedContent() ?: false

  /**
   *  @return Progress completion for content
   */
  fun getProgressionPercentComplete(): Int = relationships?.getPercentComplete() ?: 0

  /**
   *  @return Progress completion for content
   */
  fun getProgressionProgress(): Long = relationships?.getProgressionProgress() ?: 0

  /**
   *  Domain
   *
   *  @return content domain ex. iOS & Swift
   */
  fun getDomain(): String? = relationships?.getDomainName()

  /**
   * Set the [Contents.included] meta data to this object
   *
   * @param updatedRelations list of domains, progressions, bookmarks
   *
   * @return this instance
   */
  fun updateRelationships(updatedRelations: List<Data>?): Data {
    if (updatedRelations.isNullOrEmpty()) {
      return this
    }

    val updatedRelationships = relationships
      ?.updateDomains(updatedRelations)
      ?.updateProgression(id, updatedRelations)
      ?.updateBookmark(updatedRelations) ?: Relationships()

    return this.copy(relationships = updatedRelationships)
  }

  /**
   * Add new relationships to data
   */
  fun addRelationships(newRelations: List<Data>?): Data {
    if (newRelations.isNullOrEmpty()) {
      return this
    }

    val newRelationShips = relationships
      ?.addDomains(newRelations)
      ?.addProgression(newRelations)
      ?.addBookmark(newRelations)
      ?.addContents(newRelations)
      ?.addContentGroups(newRelations)
      ?: Relationships()
        .addDomains(newRelations)
        .addProgression(newRelations)
        .addBookmark(newRelations)
        .addContents(newRelations)
        .addContentGroups(newRelations)

    return this.copy(relationships = newRelationShips)
  }

  /**
   *  @return bookmark id
   */
  fun getBookmarkId(): String? = relationships?.getBookmarkId()

  /**
   * @return progression id
   */
  fun getProgressionId(): String? = relationships?.getProgressionId()

  /**
   * Add bookmark relation and attribute from content
   *
   * @param bookmark Bookmark to be added to relationship
   *
   * @return Data after adding bookmark
   */
  fun addBookmark(bookmark: Content?): Data {
    val relationships =
      this.relationships?.copy(bookmark = bookmark) ?: Relationships(bookmark = bookmark)
    return this.copy(attributes = attributes, relationships = relationships)
  }

  /**
   * Add bookmark to content
   */
  fun addBookmark(bookmarkId: String?): Data {
    val relationships =
      this.relationships?.addBookmark(bookmarkId = bookmarkId)
        ?: Relationships().addBookmark(bookmarkId = bookmarkId)
    return this.copy(attributes = attributes, relationships = relationships)
  }

  /**
   *  Remove bookmark relation and attribute from content
   *
   *  @return Data after removing bookmark
   */
  fun removeBookmark(): Data {
    val relationships = this.relationships?.copy(bookmark = null)
    return this.copy(attributes = attributes, relationships = relationships)
  }

  /**
   *  Remove download from data
   *
   *  @return Data after removing bookmark
   */
  fun removeDownload(): Data {
    return this.copy(download = null)
  }

  /**
   *  @return true if type is [DataType.Groups], otherwise false
   */
  fun isTypeGroup(): Boolean = DataType.Groups == DataType.fromValue(type)

  /**
   *  @return true if type is [ContentType.Screencast], otherwise false
   */
  fun isTypeScreencast(): Boolean = getContentType()?.isScreencast() ?: false

  /**
   *  @return formatted episode duration string
   */
  fun getEpisodeDuration(): String {
    val (hrs, mins, secs) = attributes?.getDurationHoursAndMinutesAndSeconds() ?: return ""
    if (hrs <= 0) {
      return "${"%02d".format(mins)}:${"%02d".format(secs)}"
    }
    return "${"%02d".format(hrs)}:${"%02d".format(mins)}:${"%02d".format(secs)}"
  }

  /**
   *  @return grouped data list
   */
  fun getChildContents(): List<Data> = relationships?.getChildContents() ?: emptyList()

  /**
   *  @return ids of grouped data list
   */
  fun getChildContentIds(): List<String> = relationships?.getChildContentIds() ?: emptyList()

  /**
   * Mark episode finished/ or in-progress
   */
  fun updateProgressionFinished(contentId: String, finished: Boolean): Data {
    val relationships =
      this.relationships?.updateProgressionFinished(contentId, finished)
        ?: Relationships().updateProgressionFinished(contentId, finished)
    return this.copy(relationships = relationships)
  }

  /**
   * Mark episode finished/ or in-progress
   */
  fun toggleFinished(contentId: String, finished: Boolean): Data {
    val percentComplete = if (finished) {
      100.0
    } else {
      0.0
    }
    return this.copy(
      attributes = this.attributes?.copy(percentComplete = percentComplete, finished = finished)
        ?: Attributes(finished = finished), relationships = relationships ?: Relationships(
        content = Content(
          datum = Data(id = contentId)
        )
      )
    )
  }


  /**
   * Get episode number
   *
   * @param position Episode position
   * @param playbackAllowed Episode playback allowed
   *
   * @return Empty String if episode is finished or it requires subscription,
   * else String of position
   */
  fun getEpisodeNumber(position: Int, playbackAllowed: Boolean): String =
    if (!playbackAllowed || isProgressionFinished()) "" else position.toString()

  /**
   *  @return content id for relationships
   */
  fun getContentId(): String = relationships?.getContentId() ?: ""

  /**
   * Get list of domain ids from relationships
   *
   * @return list of domain ids
   */
  fun getDomainIds(): List<String> = relationships?.getDomainIds() ?: emptyList()

  /**
   * Get video id
   *
   * @return video id string
   */
  fun getVideoId(): String? = attributes?.videoId

  /**
   *  @return stream/download url for content
   */
  fun getUrl(): String = attributes?.url ?: download?.url ?: ""

  /**
   * @return Video playback token for user
   */
  fun getVideoPlaybackToken(): String = attributes?.videoPlaybackToken ?: ""

  /**
   * @return set video url on current data item
   */
  fun setVideoUrl(data: Data?): Data? {
    return this.copy(attributes = attributes?.setVideoUrl(data?.attributes))
  }

  /**
   * @return Tag for data
   */
  fun getTag(): String? = attributes?.tag

  /**
   * @return list of content group ids
   */
  fun getContentGroupIds(): List<String> {
    return this.relationships?.getContentGroupIds() ?: emptyList()
  }

  /**
   * Check if content is downloaded
   *
   * @return true if content is downloaded, else false
   */
  fun isDownloaded(): Boolean = download.isDownloaded()

  /**
   * Check if content is cached, but not downloaded
   *
   * @return true if content is cached, else false
   */
  fun isCached(): Boolean = download.isCached()

  /**
   * Check if content is downloading
   *
   * @return true if content is downloading, else false
   */
  fun isDownloading(): Boolean = download.isDownloading()

  /**
   * Check if content is not downloaded and is either in progress or has failed.
   *
   * @return true if content is not downloaded, else false
   */
  fun isNotDownloaded(): Boolean =
    download.isDownloading() || download.isFailed() || download.isPending() || download.isPaused()

  /**
   * Get content download progress
   *
   * @return true if content is downloaded, else false
   */
  fun getDownloadProgress(): Int = download.getProgress()

  /**
   * Get download state
   *
   * @return get download state
   */
  fun getDownloadState(): Int = download.getState()

  /**
   * Update content download progress
   *
   * @return Updated [Data]
   */
  fun updateDownloadProgress(download: Download?): Data {
    return this.copy(download = download)
  }

  /**
   * Create [Progression] from [Data]
   *
   * @param contentId Content id for progression
   */
  fun toProgression(contentId: String): Progression = Progression(
    contentId = contentId,
    progressionId = id,
    percentComplete = getPercentComplete(),
    finished = isFinished(),
    updatedAt = getUpdatedAt(),
    synced = true
  )


  /**
   * Create [Progression] from response [Data]
   */
  fun toProgression(): Progression = Progression(
    contentId = getContentId(),
    progressionId = id,
    progress = getProgress(),
    percentComplete = getPercentComplete(),
    finished = isFinished(),
    updatedAt = getUpdatedAt(),
    synced = true
  )

  /**
   * Get updated at
   */
  fun getUpdatedAt(): String = attributes?.updatedAt ?: ""

  companion object {

    /**
     *  @param dataList List of content
     *
     *  @return list of domain ids from input list
     */
    fun getDomainIds(dataList: List<Data>): List<String> {
      return dataList.filter { FilterType.fromType(it.type).isDomain() }
        .mapNotNull { it.id }
    }

    /**
     *  @param dataList List of content
     *
     *  @return list of category ids from input list
     */
    fun getCategoryIds(dataList: List<Data>): List<String> {
      return dataList.filter { FilterType.fromType(it.type).isCategory() }
        .mapNotNull { it.id }
    }

    /**
     *  @param dataList List of content
     *
     *  @return list of category ids from input list
     */
    fun getSearchTerm(dataList: List<Data>): String {
      return dataList.firstOrNull { FilterType.fromType(it.type).isSearch() }?.getName()
        ?: ""
    }

    /**
     *  @param dataList List of content type
     *
     *  @return list of content types from input list
     */
    fun getContentTypes(dataList: List<Data>): List<String> {
      return dataList.filter { FilterType.fromType(it.type).isContentType() }
        .filter {
          !it.getContentType().isProfessional()
        }
        .mapNotNull { it.getContentType()?.toString()?.toLowerCase() }
    }

    /**
     *  @param dataList List of difficulty
     *
     *  @return list of difficulty from input list
     */
    fun getDifficulty(dataList: List<Data>): List<String> {
      return dataList.filter { FilterType.fromType(it.type).isDifficulty() }
        .mapNotNull { it.getName()?.toLowerCase() }
    }

    /**
     *  @param dataList List of content
     *
     *  @return list of category ids from input list
     */
    fun getSortOrder(dataList: List<Data>): String {
      val sortOrder =
        dataList.firstOrNull { FilterType.fromType(it.type).isSort() }?.getName()
          ?: ""
      return SortOrder.fromValue(sortOrder)?.param ?: SortOrder.Newest.param
    }

    /**
     *  @param dataList List of content
     *
     *  @return list of category ids from input list
     */
    fun getProfessional(dataList: List<Data>): Boolean? {
      if (dataList.isEmpty()) {
        return null
      }

      val hasProfessionalFilter = dataList.filter {
        FilterType.fromType(it.type).isContentType()
      }.any { it.getContentType().isProfessional() }

      return if (hasProfessionalFilter) {
        hasProfessionalFilter
      } else {
        null
      }
    }


    /**
     * Create a data object from [Category] row
     *
     * @param category Category row from database
     *
     * @return [Data] for passed category
     */
    fun fromCategory(category: Category): Data =
      Data(
        id = category.categoryId,
        type = DataType.Categories.toRequestFormat(),
        attributes = Attributes(
          name = category.name
        )
      )

    /**
     * Create a data object from [Domain] row
     *
     * @param domain Domain row from database
     *
     * @return [Data] for passed domain
     */
    fun fromDomain(domain: Domain): Data =
      Data(
        id = domain.domainId,
        type = DataType.Domains.toRequestFormat(),
        attributes = Attributes(
          name = domain.name,
          level = domain.level
        )
      )


    /**
     * Create a data object for search query
     *
     * @param searchTerm Search query
     *
     * @return [Data] for passed query
     */
    fun fromSearchQuery(searchTerm: String?): Data =
      Data(
        type = FilterType.Search.toRequestFormat(),
        attributes = Attributes(
          name = searchTerm
        )
      )

    /**
     * Create a data object for sort order
     *
     * @param sortOrder Sort Order
     *
     * @return [Data] for passed query
     */
    fun fromSortOrder(sortOrder: String?): Data =
      Data(
        type = FilterType.Sort.toRequestFormat(),
        attributes = Attributes(
          name = sortOrder
        )
      )

    /**
     * Create content object for creating new progression
     *
     * @param contentId id of content for which progression has to be created/updated
     * @param finished true if content is completed else false
     * @param progress
     * @param updatedAt Update at time
     */
    fun newProgression(
      contentId: String,
      finished: Boolean = false,
      progress: Long = 0,
      updatedAt: LocalDateTime
    ): Data =
      Data(
        type = DataType.Progressions.toRequestFormat(),
        attributes = Attributes(
          contentId = contentId,
          progress = progress,
          finished = finished,
          updatedAt = updatedAt.format(DateTimeFormatter.ISO_DATE_TIME)
        )
      )
  }
}
