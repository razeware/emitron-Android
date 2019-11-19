package com.razeware.emitron.model.entity

import com.razeware.emitron.model.Attributes
import com.razeware.emitron.model.Contents
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.Relationships
import com.razeware.emitron.model.utils.isEqualTo
import org.junit.Test

class ContentDomainJoinTest {

  @Test
  fun init() {
    val contentDomainJoin = ContentDomainJoin("1", "2")

    // Assertions
    contentDomainJoin.contentId isEqualTo "1"
    contentDomainJoin.domainId isEqualTo "2"
  }


  @Test
  fun listFrom() {
    val dataList = listOf(
      Data(
        id = "1",
        type = "contents",
        attributes = Attributes(
          createdAt = "",
          description = "In this tutorial you will learn how to use lambda.",
          level = "",
          name = "Introduction to Kotlin Lambdas: Getting Started",
          slug = "",
          cardArtworkUrl = "https://koenig-media.razeware.com/",
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
          url = "",
          kind = ""
        ),
        links = null,
        relationships = Relationships(
          content = null,
          contents = null,
          bookmark = null,
          domains = Contents(
            datum = listOf(
              Data(
                id = "1",
                type = "domains",
                attributes = Attributes(name = "iOS and Swift")
              ),
              Data(
                id = "2",
                type = "domains",
                attributes = Attributes(name = "Android and Kotlin")
              )
            )
          ),
          progression = null,
          groups = null,
          childContents = null
        ),
        meta = null,
        included = null
      )
    )

    val result = ContentDomainJoin.listFrom(dataList)
    result isEqualTo listOf(
      ContentDomainJoin("1", "1"),
      ContentDomainJoin("1", "2")
    )
  }

  @Test
  fun from() {
    val data = Data(
      id = "1",
      type = "contents",
      attributes = Attributes(
        createdAt = "",
        description = "In this tutorial you will learn how to use lambda.",
        level = "",
        name = "Introduction to Kotlin Lambdas: Getting Started",
        slug = "",
        cardArtworkUrl = "https://koenig-media.razeware.com/",
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
        url = "",
        kind = ""
      ),
      links = null,
      relationships = Relationships(
        content = null,
        contents = null,
        bookmark = null,
        domains = Contents(
          datum = listOf(
            Data(
              id = "1",
              type = "domains",
              attributes = Attributes(name = "iOS and Swift")
            ),
            Data(
              id = "2",
              type = "domains",
              attributes = Attributes(name = "Android and Kotlin")
            )
          )
        ),
        progression = null,
        groups = null,
        childContents = null
      ),
      meta = null,
      included = null
    )

    val result = ContentDomainJoin.from(data)
    result isEqualTo listOf(
      ContentDomainJoin("1", "1"),
      ContentDomainJoin("1", "2")
    )
  }
}
