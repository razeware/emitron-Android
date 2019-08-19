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

  fun getDomainName(): String? = domains?.datum?.mapNotNull { it.attributes?.name }?.joinToString()

  fun hasFinishedContent(): Boolean = progression?.isFinished() ?: false

  fun getPercentComplete(): Int = progression?.getPercentComplete() ?: 0

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
}
