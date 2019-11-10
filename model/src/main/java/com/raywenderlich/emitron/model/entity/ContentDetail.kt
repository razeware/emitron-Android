package com.raywenderlich.emitron.model.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.model.Download
import com.raywenderlich.emitron.model.DownloadState

/**
 * Relation representing a Content including it's domain, progressions, groups, episodes
 */
data class ContentDetail(

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
   * Progressions
   */
  @Relation(
    parentColumn = "content_id",
    entityColumn = "content_id",
    entity = Progression::class
  )
  val progressions: List<Progression> = emptyList(),

  /**
   * Groups
   */
  @Relation(
    parentColumn = "content_id",
    entityColumn = "content_id",
    entity = ContentGroupJoin::class
  )
  val groups: List<ContentGroupJoinWithGroup> = emptyList()
) {

  private fun getContentGroups(): List<Data> = groups.map {
    it.groups.first().toData().addRelationships(
      it.episodes.map { episode -> episode.episodes.first().toGroupData() }
    )
  }

  private fun getContentEpisodes(): List<Data> = groups.flatMap {
    it.episodes.map { episode -> episode.episodes.first().toData() }
  }

  private fun getContentDomains(): List<Data> =
    domains.flatMap {
      it.domains.map { domain ->
        domain.toData()
      }
    }

  private fun getContentEpisodesProgressions(): List<Data> =
    groups.flatMap { contentGroupJoin ->
      contentGroupJoin.episodes.flatMap { groupEpisodeJoin ->
        groupEpisodeJoin.episodes.flatMap { contentWithDomainAndProgression ->
          contentWithDomainAndProgression.progressions.map { progression -> progression.toData() }
        }
      }
    }

  /**
   * Change [ContentDetail] to [com.raywenderlich.emitron.model.Content]
   */
  fun toContent(): com.raywenderlich.emitron.model.Content {

    val domains = getContentDomains()

    val groups = getContentGroups()
    val groupsAndDomains = if (groups.isNotEmpty()) {
      domains.plus(groups)
    } else {
      domains
    }

    val episodes = getContentEpisodes()
    val groupsAndEpisodes = if (episodes.isNotEmpty()) {
      groupsAndDomains.plus(episodes)
    } else {
      groupsAndDomains
    }

    val episodeProgressions = getContentEpisodesProgressions()

    val included = if (episodeProgressions.isNotEmpty()) {
      groupsAndEpisodes.plus(episodeProgressions)
    } else {
      groupsAndEpisodes
    }

    val contentProgressions = listOf(progressions.first().toData())

    return com.raywenderlich.emitron.model.Content(
      datum = content.toData().addRelationships(domains.plus(groups).plus(contentProgressions)),
      included = included
    )
  }

  /**
   * Is content screencast or episode
   */
  fun isScreencastOrEpisode(): Boolean = content.isScreencastOrEpisode()

  /**
   * Get [Download] for [ContentDetail]
   */
  fun getDownload(): Download? {
    val episodeDownloads = groups.flatMap { contentGroupJoin ->
      contentGroupJoin.episodes.flatMap { groupEpisodeJoin ->
        groupEpisodeJoin.episodes.flatMap { contentWithDomainAndProgression ->
          contentWithDomainAndProgression.downloads
        }
      }
    }

    return if (episodeDownloads.isNotEmpty()) {
      val downloadProgress: Pair<Int, Int> = when {
        episodeDownloads.any { it.inProgress() } -> {
          episodeDownloads.map {
            it.progress
          }.reduce { acc, i ->
            i + acc
          } to DownloadState.IN_PROGRESS.ordinal
        }
        episodeDownloads.all { it.isCompleted() } -> {
          100 to DownloadState.COMPLETED.ordinal
        }
        else -> {
          0 to DownloadState.IN_PROGRESS.ordinal
        }
      }

      Download(
        progress = downloadProgress.first,
        state = downloadProgress.second
      )
    } else {
      null
    }
  }

  /**
   * Get download ids for collection
   */
  fun getDownloadIds(): List<String> = groups.flatMap { contentGroupJoin ->
    contentGroupJoin.episodes.flatMap { groupEpisodeJoin ->
      groupEpisodeJoin.episodes.flatMap { contentWithDomainAndProgression ->
        contentWithDomainAndProgression.downloads.map { (downloadId) ->
          downloadId
        }
      }
    }
  }
}
