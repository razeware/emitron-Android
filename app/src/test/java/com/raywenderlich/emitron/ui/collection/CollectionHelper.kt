package com.raywenderlich.emitron.ui.collection

import com.raywenderlich.emitron.model.*

internal fun createContentData(
  id: String = "1",
  type: String = "collection",
  groups: Contents? = Contents(datum = (1..2).map { Data(id = it.toString(), type = "groups") }),
  bookmark: Content? = null,
  isFree: Boolean = false
) = Data(
  id = id,
  type = "contents",
  attributes = Attributes(contentType = type, free = isFree),
  relationships = Relationships(
    groups = groups,
    bookmark = bookmark
  )
)

internal fun removeBookmark(data: Data) = data.copy(
  relationships = data.relationships?.copy(bookmark = null)
)

internal fun createContent(
  data: Data = createContentData(),
  included: List<Data> = emptyList()
): Content = Content(
  datum = data,
  included = included
)

internal fun getIncludedDataForCollection() = listOf(
  createGroup(1, "one", 5),
  createGroup(2, "two", 7),
  createEpisode(5, "five", relationships = createRelationship()),
  createEpisode(6, "six"),
  createEpisode(7, "seven"),
  createEpisode(8, "eight"),
  Data(id = "9", type = "progressions", attributes = Attributes(percentComplete = 10.0))
)

internal fun createRelationship() = Relationships(progression = Content(datum = Data(id = "9")))

internal fun createGroup(id: Int, name: String, dataId: Int) = Data(
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

internal fun createEpisode(id: Int, name: String, relationships: Relationships? = null) = Data(
  id = id.toString(),
  type = "contents",
  attributes = Attributes(name = name),
  relationships = relationships
)

internal fun createBookmarkResponse() = createContent(
  data = createContentData(
    id = "10",
    bookmark = Content(
      datum = Data(
        id = "10",
        type = "bookmarks"
      )
    )
  )
)

