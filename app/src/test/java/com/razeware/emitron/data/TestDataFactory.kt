package com.razeware.emitron.data

import com.razeware.emitron.model.*
import com.razeware.emitron.model.entity.*
import com.razeware.emitron.model.entity.Content
import com.razeware.emitron.model.entity.Download

/**
 * Factory function to test downloads
 */
fun createContent(
  type: String = "screencast"
): com.razeware.emitron.model.Content =
  com.razeware.emitron.model.Content(
    datum = buildContentData(
      withRelationship(
        withRelatedBookmark(),
        withRelatedDomains(),
        groups = withGroups(withGroupContents()),
        progressions = withRelatedProgression()
      ),
      contentType = type
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
        download = withDownload()
      ),
      withProgression()
    )
  )

fun createContentWithDownload(
  type: String = "screencast"
): com.razeware.emitron.model.Content =
  com.razeware.emitron.model.Content(
    datum = buildContentData(
      withRelationship(
        withRelatedBookmark(),
        withRelatedDomains(),
        groups = withGroups(withGroupContents()),
        progressions = withRelatedProgression()
      ),
      contentType = type,
      download = withDownload().copy(
        progress = 100,
        state = 3,
        url = null,
        cached = true
      )
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
        download = withDownload()
      ),
      withProgression()
    )
  )

fun createContentDetail(contentType: String = "screencast"): ContentDetail = ContentDetail(
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
  contentType: String = "screencast"
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
    kind = null,
    professional = false,
    free = false
  ),
  relationships = relationships,
  download = download
)

fun withRelatedProgression(datum: Data? = withProgression()): com.razeware.emitron.model.Content =
  com.razeware.emitron.model.Content(
    datum = datum
  )

fun withProgression(
  percentComplete: Double = 99.0,
  finished: Boolean = true,
  progress: Long = 0
): Data = Data(
  id = "1",
  type = "progressions",
  attributes = Attributes(
    percentComplete = percentComplete,
    finished = finished,
    progress = progress,
    contentId = "1"
  ),
  relationships = Relationships(
    content = com.razeware.emitron.model.Content(
      datum = Data(id = "1")
    )
  )
)

fun withDownload(): com.razeware.emitron.model.Download =
  com.razeware.emitron.model.Download(
    progress = 25,
    state = 3,
    failureReason = 0,
    url = "download/1"
  )

fun withRelatedBookmark(): com.razeware.emitron.model.Content =
  com.razeware.emitron.model.Content(
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

fun withGroupContents(datum: List<Data> = listOf(buildContentGroupData())): Contents = Contents(
  datum = datum
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

fun createDownload(state: Int = DownloadState.COMPLETED.ordinal): Download = Download(
  "1",
  "download/1",
  25,
  state,
  0,
  "createdAt"
)

/**
 * Factory function to test Content detail view
 */
fun createContentData(
  id: String = "1",
  type: String = "collection",
  groups: Contents? = Contents(datum = (1..2).map { Data(id = it.toString(), type = "groups") }),
  bookmark: com.razeware.emitron.model.Content? = null,
  professional: Boolean = false,
  videoId: Int = 1,
  videoUrl: String? = null,
  playbackToken: String = "",
  progress: Long = 0,
  download: com.razeware.emitron.model.Download? = null,
  progression: com.razeware.emitron.model.Content = withRelatedProgression()
): Data = Data(
  id = id,
  type = "contents",
  attributes = Attributes(
    name = "Introduction to Kotlin Lambdas",
    description = "Lambda expression is simplified representation of a function.",
    cardArtworkUrl = "https://koenig-media.razeware.com/KotlinLambdas-feature.png",
    contentType = type,
    professional = professional,
    videoId = videoId.toString(),
    videoPlaybackToken = playbackToken,
    url = videoUrl,
    progress = progress,
    duration = 10
  ),
  relationships = Relationships(
    groups = groups,
    bookmark = bookmark,
    progression = progression
  ),
  download = download
)

fun removeBookmark(data: Data): Data = data.copy(
  relationships = data.relationships?.copy(bookmark = null)
)

fun createContent(
  data: Data = createContentData(),
  included: List<Data> = emptyList()
): com.razeware.emitron.model.Content = com.razeware.emitron.model.Content(
  datum = data,
  included = included
)

fun getIncludedDataForCollection(): List<Data> = listOf(
  createGroup(1, "one", 5),
  createGroup(2, "two", 7),
  createEpisode(
    5,
    "five",
    relationships = createRelationship()
  ),
  createEpisode(6, "six"),
  createEpisode(7, "seven"),
  createEpisode(8, "eight"),
  Data(
    id = "9", type = "progressions", attributes = Attributes(
      percentComplete = 10.0
    )
  )
)

fun createRelationship(): Relationships = Relationships(
  progression = com.razeware.emitron.model.Content(
    datum = Data(id = "9")
  )
)

fun createGroup(id: Int, name: String, dataId: Int): Data = Data(
  id = id.toString(),
  type = "groups",
  attributes = Attributes(name = name),
  relationships = Relationships(
    contents = Contents(
      datum = listOf(
        Data(id = dataId.toString(), type = "contents"),
        Data(id = (dataId + 1).toString(), type = "contents")
      )
    )
  )
)

fun createEpisode(id: Int, name: String, relationships: Relationships? = null): Data = Data(
  id = id.toString(),
  type = "contents",
  attributes = Attributes(name = name),
  relationships = relationships
)

fun createBookmarkResponse(): com.razeware.emitron.model.Content = createContent(
  data = createContentData(
    id = "10",
    bookmark = com.razeware.emitron.model.Content(
      datum = Data(
        id = "10",
        type = "bookmarks"
      )
    )
  )
)
