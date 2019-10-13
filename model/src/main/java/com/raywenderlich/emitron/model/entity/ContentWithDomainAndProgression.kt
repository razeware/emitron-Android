package com.raywenderlich.emitron.model.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.model.DownloadState

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
    parentColumn = "progression_id",
    entityColumn = "progression_id",
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

  fun getDownloadState() = if (downloads.isNotEmpty()) {
    when {
      downloads.any { it.inProgress() } -> {
        downloads.map {
          it.progress
        }.reduce { acc, i ->
          i + acc
        } to DownloadState.IN_PROGRESS.ordinal
      }
      downloads.all { it.isCompleted() } -> {
        100 to DownloadState.COMPLETED.ordinal
      }
      else -> {
        0 to DownloadState.IN_PROGRESS.ordinal
      }
    }
  } else {
    0 to DownloadState.NONE.ordinal
  }

  fun toGroupData(): Data = content.toGroupData()
}
