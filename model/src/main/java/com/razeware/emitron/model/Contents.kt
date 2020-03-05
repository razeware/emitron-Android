package com.razeware.emitron.model

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

/**
 * Model for list response of Bookmark, Content, Progressions
 */
@JsonClass(generateAdapter = true)
@Parcelize
data class Contents(
  /**
   * List of content
   */
  @Json(name = "data")
  val datum: List<Data> = emptyList(),

  /**
   * Related content referred in list of contents
   */
  val included: List<Data>? = null,

  /**
   * Related links
   */
  val links: Links? = null,

  /**
   * Related meta data
   */
  val meta: Meta? = null
) : Parcelable {

  /**
   * @return total item count
   */
  fun getTotalCount(): Int = meta?.totalResultCount ?: 0

  /**
   * @return Next page no.
   */
  fun getNextPage(): Int? = links?.getNextPage()

  /**
   * @return list of [Data.id] for included content items
   */
  fun getChildIds(): List<String> = datum.mapNotNull { it.id }

  /**
   * Get download url from response
   *
   * @param qualityHd Download url kind
   *
   * @return download url from [Contents.datum]
   */
  fun getDownloadUrl(qualityHd: Boolean): String? {
    return if (qualityHd) {
      getHdUrl()
    } else {
      getSdUrl()
    }?.getUrl()
  }

  private fun getHdUrl(): Data? = datum.first {
    DownloadQuality.fromKind(it.attributes?.kind).isHd()
  }

  private fun getSdUrl(): Data? = datum.first {
    DownloadQuality.fromKind(it.attributes?.kind).isSd()
  }

  /**
   * Get datum with updated relationships
   */
  fun getDatumWithRelationships(): List<Data> = datum.map {
    it.updateRelationships(included)
  }

  companion object {

    /**
     * Create contents item from data items
     */
    fun from(vararg data: Data): Contents = Contents(datum = data.toList())
  }
}
