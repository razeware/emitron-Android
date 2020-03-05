package com.razeware.emitron.model

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

/**
 * Relationships
 */
@JsonClass(generateAdapter = true)
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
   * @return true/false if content is finished
   */
  fun hasFinishedContent(): Boolean = progression?.isFinished() ?: false

  /**
   * @return percent completion for progression
   */
  fun getPercentComplete(): Int = progression?.getPercentComplete() ?: 0

  /**
   * @return progression progress for content
   */
  fun getProgressionProgress(): Long = progression?.getProgress() ?: 0

  /**
   * Update domains to existing [Data.relationships] with domains
   *
   * @param domainRelations list of domains
   *
   * @return Updated relationship
   */
  fun updateDomains(domainRelations: List<Data>): Relationships {
    val domains = domainRelations.filter {
      it.isTypeDomain()
    }

    if (domains.isEmpty()) {
      return this
    }

    return run {
      val domainIds = this.domains?.getChildIds() ?: emptyList()
      val filteredDomainList = domains.filter { domainIds.contains(it.id) }
      this.copy(domains = Contents(datum = filteredDomainList))
    }
  }

  /**
   * Add domains to existing [Data.relationships] with no domains
   *
   * @param domainRelations list of domains
   *
   * @return Updated relationship
   */
  fun addDomains(domainRelations: List<Data>): Relationships {
    val domains = domainRelations.filter {
      it.isTypeDomain()
    }

    if (domains.isEmpty()) {
      return this
    }

    return this.copy(domains = Contents(datum = domains))
  }

  /**
   * Add child contents to existing [Data.relationships] with no contents
   *
   * @param contentRelations list of contents
   *
   * @return Updated relationship
   */
  fun addContents(contentRelations: List<Data>): Relationships {
    val contents = contentRelations.filter {
      it.isTypeContent()
    }

    if (contents.isEmpty()) {
      return this
    }

    return this.copy(contents = Contents(datum = contents))
  }

  /**
   * Add content groups to existing [Data.relationships] with no groups
   *
   * @param groupRelations list of groups
   *
   * @return Updated relationship
   */
  fun addContentGroups(groupRelations: List<Data>): Relationships {
    val contents = groupRelations.filter {
      it.isTypeGroup()
    }

    if (contents.isEmpty()) {
      return this
    }

    return this.copy(groups = Contents(datum = contents))
  }

  /**
   * Update progressions to existing [Data.relationships] with progressions
   *
   * @param progressionRelations list of progressions
   *
   * @return Updated relationship
   */
  fun updateProgression(contentId: String?, progressionRelations: List<Data>): Relationships {
    val progressions = progressionRelations.filter {
      it.isTypeProgression()
    }

    if (progressions.isEmpty()) {
      return this
    }

    val progressionId = progression?.getChildId()

    val progressionData = if (null != progressionId) {
      // Find a progression by progression id
      progressions.firstOrNull { it.id == progressionId }
    } else {
      // Find a progression by content id
      progressions.firstOrNull { it.getContentId() == contentId }
    }

    progressionData ?: return this
    return this.copy(progression = Content(datum = progressionData))
  }

  /**
   * Add progressions to existing [Data.relationships]
   *
   * @param progressionRelations list of progressions with no progressions
   *
   * @return Updated relationship
   */
  fun addProgression(progressionRelations: List<Data>): Relationships {
    val progressions = progressionRelations.filter {
      it.isTypeProgression()
    }

    if (progressions.isEmpty()) {
      return this
    }

    return this.copy(progression = Content(datum = progressions.first()))
  }

  /**
   * Updated bookmarks to existing [Data.relationships] with bookmarks
   *
   * @param bookmarkRelations list of bookmarks
   *
   * @return Updated relationship
   */
  fun updateBookmark(bookmarkRelations: List<Data>): Relationships {
    val bookmarks = bookmarkRelations.filter {
      it.isTypeBookmark()
    }

    if (bookmarks.isEmpty()) {
      return this
    }
    val bookmarkData =
      bookmarks.firstOrNull { it.id == bookmark?.getChildId() } ?: return this
    return this.copy(bookmark = Content(datum = bookmarkData))
  }

  /**
   * Add bookmarks to existing [Data.relationships] with no bookmarks
   *
   * @param bookmarkRelations list of progressions
   *
   * @return Updated relationship
   */
  fun addBookmark(bookmarkRelations: List<Data>): Relationships {
    val bookmarks = bookmarkRelations.filter {
      it.isTypeBookmark()
    }

    if (bookmarks.isEmpty()) {
      return this
    }

    return this.copy(bookmark = Content(datum = bookmarks.first()))
  }

  /**
   * Add bookmark by id to existing [Data.relationships]
   *
   * @param bookmarkId Bookmark Id
   *
   * @return Updated relationship
   */
  fun addBookmark(bookmarkId: String?): Relationships {
    val bookmark = Content(
      datum = Data(
        id = bookmarkId, type =
        DataType.Bookmarks.toRequestFormat()
      )
    )
    return this.copy(bookmark = bookmark)
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
  fun getChildContents(): List<Data> = contents?.datum ?: emptyList()

  /**
   * Return ids of grouped data list
   *
   * @return List<String>
   */
  fun getChildContentIds(): List<String> = contents?.datum?.mapNotNull { it.id } ?: emptyList()

  /**
   * Set related contents
   *
   * @param contentList List<Data> contents
   *
   * @return Relationships
   */
  fun setContents(contentList: List<Data>?): Relationships {
    if (contentList.isNullOrEmpty()) {
      return this
    }
    return this.copy(
      contents = Contents(datum = contentList)
    )
  }

  /**
   * Get child data id for content
   *
   * @return content id
   */
  fun getContentId(): String? = content?.getChildId()

  /**
   * Get domain ids
   *
   * @return list of domain id
   */
  fun getDomainIds(): List<String>? = domains?.getChildIds()

  /**
   * Get content group ids
   *
   * @return list of content group ids
   */
  fun getContentGroupIds(): List<String> = groups?.getChildIds() ?: emptyList()

  /**
   * Toggle progression finished
   */
  fun updateProgressionFinished(contentId: String, finished: Boolean): Relationships {
    val progression = progression?.updateFinished(
      contentId,
      finished
    ) ?: Content().updateFinished(
      contentId,
      finished
    )
    return this.copy(progression = progression)
  }
}
