package com.razeware.emitron.ui.collection

import com.razeware.emitron.model.Data

/**
 * View object for episode item
 *
 * This may represent an episode or a collection header
 *
 */
data class CollectionEpisode(
  /**
   * Episode collection header
   */
  val title: String? = "",
  /**
   * Episode item data [Data]
   */
  val data: Data? = null,
  /**
   * Episode position
   *
   */
  val position: Int = 0
) {

  fun hasTitle(): Boolean = !title.isNullOrBlank()

  companion object {

    private fun getEpisodes(data: Data): List<CollectionEpisode> =
      data.getChildContents().map { childData ->
        CollectionEpisode(data = childData)
      }

    private fun getEpisodeHeader(data: Data): CollectionEpisode? {
      val name = data.getName()
      return if (!name.isNullOrBlank()) {
        CollectionEpisode(data.getName())
      } else {
        null
      }
    }

    private fun buildFromGroup(group: Data, episodeCount: Int): List<CollectionEpisode> {
      val episodes = getEpisodes(group)
      val header = getEpisodeHeader(group)
      val episodeCountCheckHeader = if (header == null) {
        episodeCount + 1
      } else {
        episodeCount
      }
      return listOfNotNull(header, *(episodes.toTypedArray()))
        .mapIndexed { index, episode ->
          episode.copy(position = (index + episodeCountCheckHeader - episodes.size))
        }
    }

    /**
     * Build episode item from [Data]
     */
    fun buildFromGroups(groups: List<Data>, included: List<Data>): List<CollectionEpisode> {
      var episodeCounter = 0
      return groups.flatMap { group ->
        // Get content id for group episodes
        val childContentIds = group.getChildContentIds()
        episodeCounter += childContentIds.size

        // Fetch contents from group with relationships from included
        val contents =
          included.filter { (id) -> id in childContentIds }
            .map { data -> data.updateRelationships(included) }

        // Update group relationship and set contents
        val updatedGroupRelationship = group.relationships?.setContents(contents)

        // Build episode item list from groups
        val groupWithContent = group.copy(relationships = updatedGroupRelationship)
        buildFromGroup(groupWithContent, episodeCounter)
      }
    }
  }
}
