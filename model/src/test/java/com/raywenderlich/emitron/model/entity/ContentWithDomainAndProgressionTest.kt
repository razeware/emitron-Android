package com.raywenderlich.emitron.model.entity

import com.google.common.truth.Truth
import com.raywenderlich.emitron.model.Attributes
import com.raywenderlich.emitron.model.Contents
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.model.Relationships
import com.raywenderlich.emitron.model.utils.isEqualTo
import org.junit.Test

class ContentWithDomainAndProgressionTest {

  @Test
  fun init() {
    val content = Content(
      contentId = "1",
      name = "Introduction to Kotlin Lambdas: Getting Started",
      description = "In this tutorial you will learn how to use lambda.",
      contributors = "Luke",
      free = false,
      deleted = false,
      contentType = "screencast",
      difficulty = "beginner",
      releasedAt = "2019-08-08T00:00:00.000Z",
      downloadProgress = 0,
      technology = "Swift, iOS",
      duration = 408,
      streamUrl = "https://koenig-media.raywenderlich.com/",
      cardArtworkUrl = "https://koenig-media.raywenderlich.com/",
      videoId = "1",
      bookmarkId = "1",
      progressionId = "1",
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
      Progression(progressionId = "1", percentComplete = 99, finished = true)
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
    val contentWithDomain = ContentWithDomainAndProgression(
      content = Content(
        contentId = "1",
        name = "Introduction to Kotlin Lambdas: Getting Started",
        description = "In this tutorial you will learn how to use lambda.",
        contributors = "Luke",
        free = false,
        deleted = false,
        contentType = "screencast",
        difficulty = "beginner",
        releasedAt = "2019-08-08T00:00:00.000Z",
        downloadProgress = 0,
        technology = "Swift, iOS",
        duration = 408,
        streamUrl = "https://koenig-media.raywenderlich.com/",
        cardArtworkUrl = "https://koenig-media.raywenderlich.com/",
        videoId = "1",
        bookmarkId = "1",
        progressionId = "1",
        updatedAt = "2019-08-08T00:00:00.000Z"
      ),
      domains = listOf(
        ContentDomainJoinWithDomain(
          domains = listOf(
            Domain(domainId = "1", name = "iOS & Swift"),
            Domain(domainId = "2", name = "Android & Kotlin")
          )
        )
      ),
      progressions = listOf(
        Progression(progressionId = "1", percentComplete = 99, finished = true)
      )
    )

    Truth.assertThat(contentWithDomain.toData()).isEqualTo(
      Data(
        id = "1",
        type = "contents",
        attributes = Attributes(
          createdAt = "",
          description = "In this tutorial you will learn how to use lambda.",
          level = "",
          name = "Introduction to Kotlin Lambdas: Getting Started",
          slug = "",
          cardArtworkUrl = "https://koenig-media.raywenderlich.com/",
          contentType = "screencast",
          difficulty = "beginner",
          duration = 408,
          free = false,
          popularity = 0.0,
          releasedAt = "2019-08-08T00:00:00.000Z",
          uri = "",
          target = 0,
          progress = 0,
          finished = false,
          percentComplete = 0.0,
          updatedAt = "",
          technology = "Swift, iOS",
          contributors = "Luke",
          url = "https://koenig-media.raywenderlich.com/",
          kind = ""
        ),
        links = null,
        relationships = Relationships(
          content = null,
          contents = null,
          bookmark = com.raywenderlich.emitron.model.Content(
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
                id = "1",
                type = "domains",
                attributes = Attributes(name = "iOS & Swift", level = null)
              ),
              Data(
                id = "2",
                type = "domains",
                attributes = Attributes(name = "Android & Kotlin", level = null)
              )
            )
          ),
          progression = com.raywenderlich.emitron.model.Content(
            datum = Data(
              id = "1",
              type = "progressions",
              attributes = Attributes(percentComplete = 99.0, finished = true),
              links = null,
              relationships = null,
              meta = null,
              included = null
            ), links = null, meta = null, included = null
          ),
          groups = null,
          childContents = null
        )
      )
    )
  }
}
