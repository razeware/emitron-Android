package com.raywenderlich.emitron.model

import android.os.Parcelable
import com.raywenderlich.emitron.model.utils.TimeUtils
import kotlinx.android.parcel.IgnoredOnParcel
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
  val relationships: Relationships? = Relationships(),
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
   *  Release date with type and duration ex. July 25 2019
   */
  @IgnoredOnParcel
  @Transient
  var releaseDateWithTypeAndDuration: String = ""

  /**
   *  Download progress if the current item is downloading
   */
  @IgnoredOnParcel
  @Transient
  var downloadProgress: Int = 0

  /**
   *  Name
   *
   *  @return content name ex. Swift UI: Working With UIKit
   */
  fun getName(): String? = attributes?.name

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
  private fun isFreeContent(): Boolean = attributes?.free ?: false

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

    return setDomain(included)
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

    return this.copy(
      relationships = relationships?.setDomains(domains) ?: Relationships().setDomains(domains)
    )
  }

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
  }
}
