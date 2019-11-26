package com.razeware.emitron.model.entity

import com.google.common.truth.Truth
import com.razeware.emitron.model.*
import com.razeware.emitron.model.Content
import com.razeware.emitron.model.Download
import com.razeware.emitron.model.utils.isEqualTo
import org.junit.Test

class ContentWithDomainAndProgressionTest {

  @Test
  fun init() {
    val content = Content(
      contentId = "1",
      name = "Introduction to Kotlin Lambdas: Getting Started",
      description = "In this tutorial you will learn how to use lambda.",
      contributors = "Luke",
      professional = false,
      deleted = false,
      contentType = "screencast",
      difficulty = "beginner",
      releasedAt = "2019-08-08T00:00:00.000Z",
      technology = "Swift, iOS",
      duration = 408,
      streamUrl = "https://koenig-media.razeware.com/",
      cardArtworkUrl = "https://koenig-media.razeware.com/",
      videoId = "1",
      bookmarkId = "1",
      updatedAt = "2019-08-08T00:00:00.000Z"
    )
    val domains = listOf(
      ContentDomainJoinWithDomain(
        domains = listOf(
          Domain(domainId = "1", name = "iOS & Swift"),
          Domain(domainId = "2", name = "Android & Kotlin")
        )
      )
    )
    val progressions = listOf(
      Progression(progressionId = "1", percentComplete = 99, finished = true, contentId = "1")
    )
    val contentWithDomain = ContentWithDomainAndProgression(
      content = content,
      domains = domains,
      progressions = progressions
    )
    contentWithDomain.content isEqualTo content
    contentWithDomain.domains isEqualTo domains
    contentWithDomain.progressions isEqualTo progressions
  }

  @Test
  fun toData() {
    val contentWithDomain = createContentWithDomainAndProgression()
    Truth.assertThat(contentWithDomain.toData()).isEqualTo(
      Data(
        id = "1",
        type = "contents",
        attributes = Attributes(
          createdAt = null,
          description = "In this tutorial you will learn how to use lambda.",
          level = null,
          name = "Introduction to Kotlin Lambdas: Getting Started",
          slug = null,
          cardArtworkUrl = "https://koenig-media.razeware.com/",
          contentType = "screencast",
          difficulty = "beginner",
          duration = 408,
          free = false,
          professional = false,
          popularity = null,
          releasedAt = "2019-08-08T00:00:00.000Z",
          uri = null,
          target = null,
          progress = null,
          finished = null,
          percentComplete = null,
          updatedAt = null,
          technology = "Swift, iOS",
          contributors = "Luke",
          kind = null
        ),
        links = null,
        relationships = Relationships(
          content = null,
          contents = null,
          bookmark = Content(
            datum = Data(
              id = "1",
              type = "bookmarks",
              attributes = null,
              links = null,
              relationships = null,
              meta = null,
              included = null
            ), links = null, meta = null, included = null
          ),
          domains = Contents(
            datum = listOf(
              Data(
                id = "2",
                type = "domains",
                attributes = Attributes(name = "Android & Kotlin", level = null)
              )
            )
          ),
          progression = Content(
            datum = Data(
              id = "1",
              type = "progressions",
              attributes = Attributes(
                percentComplete = 99.0,
                progress = 0,
                finished = true,
                contentId = "1"
              ),
              links = null,
              relationships = Relationships(
                content = Content(
                  datum = Data(id = "1")
                )
              ),
              meta = null,
              included = null
            ), links = null, meta = null, included = null
          ),
          groups = null,
          childContents = null
        ),
        download = Download(
          progress = 25,
          state = 3,
          url = "download/1"
        )
      )
    )
  }
}

fun createContentWithDomainAndProgression(): ContentWithDomainAndProgression =
  ContentWithDomainAndProgression(
    content = Content(
      contentId = "1",
      name = "Introduction to Kotlin Lambdas: Getting Started",
      description = "In this tutorial you will learn how to use lambda.",
      contributors = "Luke",
      professional = false,
      deleted = false,
      contentType = "screencast",
      difficulty = "beginner",
      releasedAt = "2019-08-08T00:00:00.000Z",
      technology = "Swift, iOS",
      duration = 408,
      streamUrl = "https://koenig-media.razeware.com/",
      cardArtworkUrl = "https://koenig-media.razeware.com/",
      videoId = "1",
      bookmarkId = "1",
      updatedAt = "2019-08-08T00:00:00.000Z"
    ),
    domains = listOf(
      ContentDomainJoinWithDomain(
        domains = listOf(
          Domain(domainId = "2", name = "Android & Kotlin")
        )
      )
    ),
    progressions = listOf(
      Progression(progressionId = "1", percentComplete = 99, finished = true, contentId = "1")
    ),
    downloads = listOf(
      Download(
        "1",
        "download/1",
        25,
        DownloadState.COMPLETED.ordinal,
        0,
        "createdAt"
      )
    )
  )
