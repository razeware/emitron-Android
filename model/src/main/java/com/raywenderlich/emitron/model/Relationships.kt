package com.raywenderlich.emitron.model

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

/**
 * Relationships
 */
@Parcelize
data class Relationships(
  /**
   * Related content
   */
  val content: Content? = null,

  /**
   * Related contents
   */
  val contents: Contents? = null,

  /**
   * Related bookmark
   */
  val bookmark: Content? = null,

  /**
   * Related domains
   */
  val domains: Contents? = null,

  /**
   * Related progression
   */
  val progression: Content? = null,

  /**
   * Related groups
   */
  val groups: Contents? = null,

  /**
   * Related child contents
   */
  @Json(name = "child_contents")
  val childContents: Content? = null
) : Parcelable {

  /**
   * Return child data id of progression
   *
   * @return progression id
   */
  fun getDomainName(): String? = domains?.datum?.mapNotNull { it.attributes?.name }?.joinToString()

  /**
   * Return child data id of progression
   *
   * @return progression id
   */
  fun hasFinishedContent(): Boolean = progression?.isFinished() ?: false

  /**
   * Return child data id of progression
   *
   * @return progression id
   */
  fun getPercentComplete(): Int = progression?.getPercentComplete() ?: 0

  /**
   * Set domains
   *
   * @return domainList List<Data> list of domains
   */
  fun setDomains(domainList: List<Data>): Relationships {
    if (domainList.isEmpty()) {
      return this
    }

    return run {
      val domainIds = this.domains?.getDomainIds() ?: emptyList()
      val filteredDomainList = domainList.filter { domainIds.contains(it.id) }
      this.copy(domains = Contents(datum = filteredDomainList))
    }
  }

  /**
   * Set progressions
   *
   * @return domainList List<Data> list of progressions
   */
  fun setProgression(progressions: List<Data>): Relationships {
    if (progressions.isEmpty()) {
      return this
    }
    val progressionData =
      progressions.firstOrNull { it.id == progression?.getChildId() } ?: return this
    return this.copy(progression = Content(datum = progressionData))
  }

  /**
   * Return child data id of bookmark
   *
   * @return bookmark id
   */
  fun getBookmarkId(): String? = bookmark?.getChildId()

  /**
   * Return child data id of progression
   *
   * @return progression id
   */
  fun getProgressionId(): String? = progression?.getChildId()

  /**
   * Return grouped data list
   *
   * @return List<Data>
   */
  fun getGroupedData(): List<Data> = contents?.datum ?: emptyList()

  /**
   * Return ids of grouped data list
   *
   * @return List<String>
   */
  fun getGroupedDataIds(): List<String> = contents?.datum?.mapNotNull { it.id } ?: emptyList()

  /**
   * Set related contents
   *
   * @param contentList List<Data> contents
   *
   * @return Relationships
   */
  fun setContents(contentList: List<Data>?): Relationships? {
    if (contentList.isNullOrEmpty()) {
      return null
    }
    return this.copy(
      contents = Contents(datum = contentList)
    )
  }
}
