package com.razeware.emitron.model.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.DownloadState

/**
 * Relation representing a Content including it's domain, progressions, groups, episodes
 */
data class DownloadWithContent(

  /**
   * Content
   */
  @Embedded
  val download: Download,

  /**
   * Domains
   */
  @Relation(
    parentColumn = "download_id",
    entityColumn = "content_id",
    entity = Content::class
  )
  val contents: List<ContentDetail> = emptyList()
) {

  /**
   * Get video Id
   */
  fun getVideoId(): String? =
    if (contents.isNotEmpty()) contents.first().content.videoId else null

  /**
   * Get content Id
   */
  fun getContentId(): String? =
    if (contents.isNotEmpty()) contents.first().content.contentId else null

  /**
   * Get download Id
   */
  fun getDownloadId(): String = download.downloadId

  /**
   * Get content name
   */
  fun getContentName(): String? =
    if (contents.isNotEmpty()) contents.first().content.name else null

  /**
   * Get Download Id
   */
  fun getDownloadIds(): List<String> {
    val content = contents.first()
    return if (content.isScreencastOrEpisode()) {
      listOf(download.downloadId)
    } else {
      content.getDownloadIds().plus(download.downloadId)
    }
  }

  /**
   * @return [Data] from [DownloadWithContent]
   */
  fun toData(): Data {
    val content = contents.first()

    val download = if (content.isScreencastOrEpisode()) {
      com.razeware.emitron.model.Download(
        download.progress,
        download.state,
        download.failureReason,
        download.url
      )
    } else {
      content.getDownload()
    }

    return contents.first().content.toData(downloadState = download).addRelationships(
      contents.first().domains.flatMap {
        it.domains.map { domain ->
          domain.toData()
        }
      })
  }
}

/**
 * Is download in progress
 */
fun DownloadWithContent?.inProgress(): Boolean =
  this?.download?.inProgress() ?: false

/**
 * Is download paused
 */
fun DownloadWithContent?.isPaused(): Boolean =
  this?.download?.isPaused() ?: false

/**
 * Is download completed
 */
fun DownloadWithContent?.isCompleted(): Boolean {
  if (this?.contents.isNullOrEmpty()) return false
  val isScreencastOrEpisode =
    this?.contents?.first()?.content?.isScreencastOrEpisode() ?: false
  return if (isScreencastOrEpisode) {
    this?.download?.state == DownloadState.COMPLETED.ordinal
  } else {
    this?.contents?.first()?.groups?.all { contentGroupJoinWithGroup ->
      contentGroupJoinWithGroup.episodes.all { groupEpisodeJoinWithEpisode ->
        groupEpisodeJoinWithEpisode.episodes.all { contentWithDomainAndProgression ->
          val downloads = contentWithDomainAndProgression.downloads
          if (downloads.isEmpty()) {
            false
          } else {
            downloads.first().isCompleted()
          }
        }
      }
    } ?: false
  }
}

/**
 * Is download failed
 */
fun DownloadWithContent?.hasFailed(): Boolean =
  this?.download?.state == DownloadState.FAILED.ordinal
