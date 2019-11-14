package com.raywenderlich.emitron.model.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.raywenderlich.emitron.model.Data

/**
 * Relation model for joining content, domain and progression
 */
data class ContentWithDomainAndProgression(

  /**
   * Content
   */
  @Embedded
  val content: Content,

  /**
   * Domains
   */
  @Relation(
    parentColumn = "content_id",
    entityColumn = "content_id",
    entity = ContentDomainJoin::class
  )
  val domains: List<ContentDomainJoinWithDomain> = emptyList(),

  /**
   * Progression
   */
  @Relation(
    parentColumn = "content_id",
    entityColumn = "content_id",
    entity = Progression::class
  )
  val progressions: List<Progression> = emptyList(),

  /**
   * Downloads
   */
  @Relation(
    parentColumn = "content_id",
    entityColumn = "download_id",
    entity = Download::class
  )
  val downloads: List<Download> = emptyList()
) {

  /**
   * @return [Data] from [ContentWithDomainAndProgression]
   */
  fun toData(): Data {
    val domains = domains.flatMap {
      it.domains.map { domain ->
        domain.toData()
      }
    }

    val relationships = if (progressions.isNotEmpty()) {
      domains.plus(progressions.first().toData())
    } else {
      domains
    }

    val data = if (downloads.isNotEmpty()) {
      val download = com.raywenderlich.emitron.model.Download(
        progress = downloads.first().progress,
        state = downloads.first().state,
        url = downloads.first().url,
        failureReason = downloads.first().failureReason
      )

      content.toData(download)
    } else {
      content.toData()
    }

    return data.addRelationships(relationships)
  }

  fun toGroupData(): Data = content.toGroupData()
}
