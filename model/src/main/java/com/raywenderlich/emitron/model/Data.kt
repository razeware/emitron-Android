package com.raywenderlich.emitron.model

import android.os.Parcelable
import com.raywenderlich.emitron.model.utils.TimeUtils
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.Clock
import org.threeten.bp.LocalDateTime

/**
 *  Model class for Bookmark, Domain, Progression, Content, Group.
 */
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
  val included: Contents? = null
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
   *  @return true if type is [DataType.Progressions], else false
   */
  private fun isTypeProgression(): Boolean = DataType.Progressions == DataType.fromValue(this.type)

  /**
   *  @return true if type is [DataType.Domains], else false
   */
  private fun isTypeDomain(): Boolean = DataType.Domains == DataType.fromValue(type)

  /**
   *  @return content description
   */
  fun getDescription(): String? = attributes?.description

  /**
   *  @return card artwork url for content
   */
  fun getCardArtworkUrl(): String? = attributes?.cardArtworkUrl

  /**
   *  @return true if content doesn't require subscription, else false
   */
  fun isFreeContent(): Boolean = attributes?.free ?: false

  /**
   *  If data represents a progression object
   *
   *  @return percent completion value for progression
   */
  fun getPercentComplete(): Int = attributes?.getPercentComplete() ?: 0

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
  fun isFinished(): Boolean = attributes?.finished ?: false || isProgressionFinished()

  /**
   *  @return true if content requires subscription, else false
   */
  fun isProLabelVisible(): Boolean =
    !isTypeProgression() && !isFreeContent() && !isFinished()

  /**
   *  @return [TimeUtils.Day] after parsing release date of content
   */
  fun getReleasedAt(
    withYear: Boolean, today: LocalDateTime = LocalDateTime.now(Clock.systemUTC())
  ): TimeUtils.Day =
    attributes?.getReadableReleasedAt(withYear, today) ?: TimeUtils.Day.None

  /**
   *  @return [Pair] of hours, minutes for watch duration content
   */
  fun getDuration(): Pair<Long, Long> = attributes?.getDurationHoursAndMinutes() ?: 0L to 0L

  /**
   *  @return true if content is bookmarked, else false
   */
  fun isBookmarked(): Boolean = attributes?.bookmarked ?: false

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
   *  @return true if content doesn't require subscription, else false
   */
  fun getProgressionPercentComplete(): Int? = relationships?.getPercentComplete() ?: 0

  /**
   *  Domain
   *
   *  @return content domain ex. iOS & Swift
   */
  fun getDomain(): String? = relationships?.getDomainName()

  /**
   * Set the [Contents.included] meta data to this object
   *
   * @param included list of domains, progressions, bookmarks
   *
   * @return this instance
   */
  fun setIncluded(included: List<Data>?): Data {
    if (included.isNullOrEmpty()) {
      return this
    }

    return setDomain(included).setProgression(included)
  }

  /**
   * Set the [Contents.included] domains to this object
   *
   * @param included list of domains
   *
   * @return this instance
   */
  private fun setDomain(included: List<Data>): Data {
    val domains = included.filter {
      it.isTypeDomain()
    }

    if (domains.isEmpty()) {
      return this
    }

    val relationships =
      relationships?.setDomains(domains) ?: Relationships().setDomains(domains)

    return this.copy(relationships = relationships)
  }

  private fun setProgression(included: List<Data>): Data {
    val progressions = included.filter {
      it.isTypeProgression()
    }

    if (progressions.isEmpty()) {
      return this
    }

    val relationships =
      relationships?.setProgression(progressions) ?: Relationships().setProgression(progressions)

    return this.copy(relationships = relationships)
  }

  /**
   *  @return true if content doesn't require subscription, otherwise false
   */
  fun getBookmarkId(): String? = relationships?.getBookmarkId()

  /**
   * @return progression id
   */
  fun getProgressionId(): String? = relationships?.getProgressionId()

  /**
   *  @return true if content doesn't require subscription, otherwise false
   */
  fun addBookmark(content: Content?): Data {
    val relationships = this.relationships?.copy(bookmark = content)
    val attributes = this.attributes?.copy(bookmarked = true) ?: Attributes(bookmarked = true)
    return this.copy(attributes = attributes, relationships = relationships)
  }

  /**
  <<<<<<< Updated upstream
   *  @return true if content doesn't require subscription, otherwise false
   */
  fun removeBookmark(): Data {
    val relationships = this.relationships?.copy(bookmark = null)
    val attributes = this.attributes?.copy(bookmarked = false)
    return this.copy(attributes = attributes, relationships = relationships)
  }

  /**
   *  @return true if type is [DataType.Groups], otherwise false
   */
  fun isTypeGroup(): Boolean = DataType.Groups == DataType.fromValue(type)

  /**
   *  @return true if type is [ContentType.Screencast], otherwise false
   */
  fun isTypeScreencast(): Boolean = getContentType()?.isScreenCast() ?: false

  /**
   *  @return true if content doesn't require subscription, otherwise false
   */
  fun getEpisodeDuration(): String {
    val (hrs, mins, secs) = attributes?.getDurationHoursAndMinutesAndSeconds() ?: return ""
    if (hrs <= 0) {
      return "${"%02d".format(mins)}:${"%02d".format(secs)}"
    }
    return "${"%02d".format(hrs)}:${"%02d".format(mins)}:${"%02d".format(secs)}"
  }

  /**
   *  @return true if content doesn't require subscription, otherwise false
   */
  fun getGroupedData(): List<Data> = relationships?.getGroupedData() ?: emptyList()

  /**
   *  @return true if content doesn't require subscription, otherwise false
   */
  fun getGroupedDataIds(): List<String> = relationships?.getGroupedDataIds() ?: emptyList()

  /**
   * Mark episode finished/ or in-progress
   */
  fun toggleFinished(): Data =
    this.copy(attributes = this.attributes?.copy(finished = !this.isFinished()))

  /**
   * Get episode number
   *
   * @param position Episode position
   * @param episodeIsProContent Episode requires subscription
   *
   * @return Empty String if episode is finished or it requires subscription,
   * else String of position
   */
  fun getEpisodeNumber(position: Int, episodeIsProContent: Boolean): String =
    if (episodeIsProContent || isFinished()) "" else position.toString()

  companion object {

    /**
     *  @param dataList List of content
     *
     *  @return list of domain ids from input list
     */
    fun getDomainIds(dataList: List<Data>): List<String> {
      return dataList.filter { it.isTypeDomain() }
        .mapNotNull { it.id }
    }

    /**
     *  @param dataList List of content
     *
     *  @return list of category ids from input list
     */
    fun getCategoryIds(dataList: List<Data>): List<String> {
      return dataList.filter { DataType.Categories == DataType.fromValue(it.type) }
        .mapNotNull { it.id }
    }

    /**
     *  @param dataList List of content
     *
     *  @return list of category ids from input list
     */
    fun getSearchTerm(dataList: List<Data>): String {
      return dataList.firstOrNull { DataType.Search == DataType.fromValue(it.type) }?.getName()
        ?: ""
    }

    /**
     *  @param dataList List of content
     *
     *  @return list of category ids from input list
     */
    fun getSortOrder(dataList: List<Data>): String {
      val sortOrder =
        dataList.firstOrNull { DataType.Sort == DataType.fromValue(it.type) }?.getName()
          ?: ""
      return SortOrder.fromValue(sortOrder)?.param ?: SortOrder.Newest.param
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
        type = DataType.Search.toRequestFormat(),
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
        type = DataType.Sort.toRequestFormat(),
        attributes = Attributes(
          name = sortOrder
        )
      )
  }
}
