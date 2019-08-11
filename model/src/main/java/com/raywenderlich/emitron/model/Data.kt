package com.raywenderlich.emitron.model

import android.os.Parcelable
import com.raywenderlich.emitron.model.utils.TimeUtils
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

/**
 *  Model class for Bookmarks, Domains, Progressions, Contents, Groups.
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
   * Default type from which this content was mapped
   */
  @IgnoredOnParcel
  @Transient
  var mappedFromType: String? = ""

  /**
   *  Name
   *
   *  @return content name ex. Swift UI: Working With UIKit
   */
  fun getName(): String? = attributes?.name

  /**
   *  Domain
   *
   *  @return content domain ex. iOS & Swift
   */
  fun getDomain(): String? = relationships?.getDomainName()

  /**
   *  @return true if type is [DataType.Progressions], otherwise false
   */
  private fun isTypeProgression(): Boolean = DataType.Progressions == DataType.fromValue(this.type)

  /**
   *  @return true if type is [DataType.Groups], otherwise false
   */
  fun isTypeGroup(): Boolean = DataType.Groups == DataType.fromValue(type)

  /**
   *  @return true if type is [DataType.Domains], otherwise false
   */
  private fun isTypeDomain(): Boolean = DataType.Domains == DataType.fromValue(type)

  /**
   *  @return true if type is [DataType.Bookmarks], otherwise false
   */
  private fun isTypeBookmark(): Boolean = DataType.Bookmarks == DataType.fromValue(type) ||
      DataType.Bookmarks == DataType.fromValue(mappedFromType)

  /**
   *  @return content description
   */
  fun getDescription(): String? = attributes?.description

  /**
   *  @return card artwork url for content
   */
  fun getCardArtworkUrl(): String? = attributes?.cardArtworkUrl

  /**
   *  @return true if content doesn't require subscription, otherwise false
   */
  fun isFreeContent(): Boolean = attributes?.free ?: false

  /**
   *  @return true if content doesn't require subscription, otherwise false
   */
  fun isProgressionFinished(): Boolean = relationships?.hasFinishedContent() ?: false

  /**
   *  @return true if content doesn't require subscription, otherwise false
   */
  fun getPercentComplete(): Int = attributes?.getPercentComplete() ?: 0

  /**
   *  @return true if content doesn't require subscription, otherwise false
   */
  fun getProgressionPercentComplete(): Int? = relationships?.getPercentComplete() ?: 0

  /**
   *  @return true if content doesn't require subscription, otherwise false
   */
  fun isFinished(): Boolean = attributes?.finished ?: false || isProgressionFinished()

  /**
   *  @return true if content doesn't require subscription, otherwise false
   */
  fun isProLabelVisible(): Boolean =
    !isTypeProgression() && !isTypeBookmark() && !isFreeContent() && !isFinished()

  /**
   *  @return true if content doesn't require subscription, otherwise false
   */
  fun getContentType(): ContentType? = attributes?.getContentType()

  /**
   *  @return true if content doesn't require subscription, otherwise false
   */
  fun getDifficulty(): Difficulty? = attributes?.getDifficulty()

  /**
   *  @return true if content doesn't require subscription, otherwise false
   */
  fun getReleasedAt(shortReleaseDate: Boolean): TimeUtils.Day =
    attributes?.getReadableReleasedAt(shortReleaseDate) ?: TimeUtils.Day.None

  /**
   *  @return true if content doesn't require subscription, otherwise false
   */
  fun getDuration(): Pair<Long, Long> = attributes?.getDurationHoursAndMinutes() ?: 0L to 0L

  /**
   *  @return true if content is bookmarked, otherwise false
   */
  fun isBookmarked(): Boolean = attributes?.bookmarked ?: false

  /**
   *  @return true if content doesn't require subscription, otherwise false
   */
  fun getTechnology(): String? = attributes?.technology

  /**
   *  @return true if content doesn't require subscription, otherwise false
   */
  fun getContributors(): String? = attributes?.contributors

  /**
   *  @return true if content doesn't require subscription, otherwise false
   */
  fun isLevelArchived(): Boolean = attributes?.isLevelArchived() ?: false

  /**
   *  @return true if content doesn't require subscription, otherwise false
   */
  fun setIncluded(included: List<Data>?): Data {
    if (included.isNullOrEmpty()) {
      return this
    }

    return setDomain(included)
  }

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

    /**
     *  @return Mocked Data
     */
    fun createMock(): Data = Data()
  }
}
