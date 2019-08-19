package com.raywenderlich.emitron.model

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class ContentsTest {

  @Test
  fun getTotalCount() {
    val meta = Meta(totalResultCount = 10)
    val contents = Contents(meta = meta)
    assertThat(contents.getTotalCount()).isEqualTo(10)
  }

  @Test
  fun getNextPage() {
    val contents = Contents(links = Links(next = null))
    assertThat(contents.getNextPage()).isNull()

    val links: Links = mock()
    whenever(links.getNextPage()).doReturn(3)
    val contents2 = Contents(links = links)
    assertThat(contents2.getNextPage()).isEqualTo(3)
  }

  @Test
  fun getDomainIds() {
    val data = (1..10).map { Data(id = it.toString()) }
    val contents = Contents(datum = data)
    assertThat(contents.getDomainIds()).isEqualTo(data.map { it.id })
  }
}
