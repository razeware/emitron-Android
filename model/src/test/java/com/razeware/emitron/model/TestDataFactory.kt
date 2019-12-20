package com.razeware.emitron.model

import com.razeware.emitron.model.entity.*
import com.razeware.emitron.model.entity.Content
import com.razeware.emitron.model.entity.Download

fun createContent(download: com.razeware.emitron.model.Download): com.razeware.emitron.model.Content =
  Content(
    datum = buildContentData(
      withRelationship(
        withRelatedBookmark(),
        withRelatedDomains(),
        groups = withGroups(withGroupContents()),
        progressions = withRelatedProgression()
      ),
      download = download
    ),
    included = listOf(
      createDomain(),
      createGroup(withGroupContents()),
      buildContentData(
        withRelationship(
          withRelatedBookmark(),
          withRelatedDomains(),
          withRelatedProgression()
        ),
        download = withDownload(),
        contentType = "episode"
      ),
      withProgression()
    )
  )

fun createContentDetail(contentType: String = "collection"): ContentDetail = ContentDetail(
  content = Content(
    contentId = "1",
    name = "Introduction to Kotlin Lambdas: Getting Started",
    description = "In this tutorial you will learn how to use lambda.",
    contributors = "Luke",
    professional = false,
    deleted = false,
    contentType = contentType,
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
      domains = listOf(Domain(domainId = "2", name = "Android & Kotlin"))
    )
  ),
  progressions = listOf(
    Progression(contentId = "1", progressionId = "1", percentComplete = 99, finished = true)
  ),
  groups = listOf(
    createGroupEpisodeJoinWithEpisode()
  )
)

fun createGroupEpisodeJoinWithEpisode(): ContentGroupJoinWithGroup =
  ContentGroupJoinWithGroup(
    groups = listOf(
      Group("1", "The basics", 1)
    ),
    episodes = listOf(
      GroupEpisodeJoinWithEpisode(
        episodes = listOf(
          createContentWithDomainAndProgression()
        )
      )
    )
  )

fun buildContentData(
  relationships: Relationships? = null,
  download: com.razeware.emitron.model.Download? = null,
  contentType: String = "collection"
): Data = Data(
  id = "1",
  type = "contents",
  attributes = Attributes(
    createdAt = null,
    description = "In this tutorial you will learn how to use lambda.",
    level = null,
    name = "Introduction to Kotlin Lambdas: Getting Started",
    slug = null,
    cardArtworkUrl = "https://koenig-media.razeware.com/",
    contentType = contentType,
    difficulty = "beginner",
    duration = 408,
    free = false,
    professional = false,
    popularity = null,
    releasedAt = "2019-08-08T00:00:00.000Z",
    videoId = null,
    target = null,
    progress = null,
    finished = null,
    percentComplete = null,
    updatedAt = null,
    technology = "Swift, iOS",
    contributors = "Luke",
    kind = null
  ),
  relationships = relationships,
  download = download
)

internal fun withRelatedProgression(): com.razeware.emitron.model.Content =
  Content(
    datum = withProgression()
  )

internal fun withProgression(): Data = Data(
  id = "1",
  type = "progressions",
  attributes = Attributes(
    percentComplete = 99.0,
    progress = 0,
    finished = true,
    contentId = "1"
  ),
  relationships = Relationships(
    content = Content(
      datum = Data(id = "1")
    )
  )
)

internal fun withDownload(): com.razeware.emitron.model.Download =
  Download(
    progress = 25,
    state = 3,
    failureReason = 0,
    url = "download/1"
  )

internal fun withRelatedBookmark(): com.razeware.emitron.model.Content =
  Content(
    datum = Data(
      id = "1",
      type = "bookmarks",
      attributes = null,
      links = null,
      relationships = null,
      meta = null,
      included = null
    ), links = null, meta = null, included = null
  )

fun createDomain(): Data = Data(
  id = "2",
  type = "domains",
  attributes = Attributes(
    name = "Android & Kotlin",
    level = null
  )
)

fun createGroup(contents: Contents? = null): Data = Data(
  id = "1",
  type = "groups",
  attributes = Attributes(
    name = "The basics",
    ordinal = 1
  ),
  relationships = Relationships(contents = contents)
)

fun withRelatedDomains(): Contents = Contents(
  datum = listOf(createDomain())
)

fun withGroups(contents: Contents? = null): Contents = Contents(
  datum = listOf(createGroup(contents))
)

fun withGroupContents(): Contents = Contents(
  datum = listOf(buildContentGroupData())
)

fun buildContentGroupData(): Data = Data(
  id = "1",
  type = "contents"
)

fun withRelationship(
  bookmark: com.razeware.emitron.model.Content? = null,
  domains: Contents? = null,
  progressions: com.razeware.emitron.model.Content? = null,
  groups: Contents? = null
): Relationships = Relationships(
  content = null,
  contents = null,
  bookmark = bookmark,
  domains = domains,
  progression = progressions,
  groups = groups,
  childContents = null
)

fun createContentWithDomainAndProgression(): ContentWithDomainAndProgression =
  ContentWithDomainAndProgression(
    content = Content(
      contentId = "1",
      name = "Introduction to Kotlin Lambdas: Getting Started",
      description = "In this tutorial you will learn how to use lambda.",
      contributors = "Luke",
      professional = false,
      deleted = false,
      contentType = "episode",
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
      Progression(contentId = "1", progressionId = "1", percentComplete = 99, finished = true)
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

fun createDownloadWithContent(
  download: Download = createDownload(),
  contentType: String = "screencast"
): DownloadWithContent = DownloadWithContent(
  download = download,
  contents = listOf(
    createContentDetail(contentType)
  )
)

internal fun createDownload(state: Int = DownloadState.COMPLETED.ordinal): Download =
  Download(
    "1",
    "download/1",
    25,
    state,
    0,
    "createdAt"
  )
