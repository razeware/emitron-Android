package com.razeware.emitron.ui.collection

import com.razeware.emitron.data.createContentData
import com.razeware.emitron.data.createGroup
import com.razeware.emitron.data.withGroupContents
import com.razeware.emitron.utils.isEqualTo
import org.junit.Test

class CollectionEpisodeTest {

  @Test
  fun buildFromGroups() {
    val data = (1..2).map {
      createGroup(
        withGroupContents(
          listOf(
            createContentData(),
            createContentData(),
            createContentData(),
            createContentData(),
            createContentData(),
            createContentData()
          )
        )
      )
    }
    val included = listOf(
      createContentData(),
      createContentData(),
      createContentData(),
      createContentData(),
      createContentData(),
      createContentData()
    )
    val episodes = CollectionEpisode.buildFromGroups(data, included)

    episodes.map { it.position } isEqualTo listOf(0, 1, 2, 3, 4, 5, 6, 6, 7, 8, 9, 10, 11, 12)
  }
}
