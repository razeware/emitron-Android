package com.razeware.emitron.model

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(Uri::class)
class LinksTest {

  private val uri: Uri = mock()

  @Before
  fun setUp() {
    PowerMockito.mockStatic(Uri::class.java)
    PowerMockito.`when`(Uri.parse(anyString())).doReturn(uri)
  }

  @Test
  fun getCurrentPage() {
    val links = Links()
    assertThat(links.getCurrentPage()).isEqualTo(0)

    val link2 = Links(self = "https://rw/contents?page[number]=1")
    whenever(uri.getQueryParameter("page[number]")).doReturn(1.toString())
    assertThat(link2.getCurrentPage()).isEqualTo(1)
  }

  @Test
  fun getNextPage() {
    val links = Links(next = null)
    assertThat(links.getNextPage()).isNull()

    val links2 = Links()
    assertThat(links2.getNextPage()).isNull()

    val link2 = Links(
      self = "https://rw/contents?page[number]=1",
      next = "https://rw/contents?page[number]=2"
    )
    whenever(uri.getQueryParameter("page[number]")).doReturn(1.toString())
    assertThat(link2.getNextPage()).isEqualTo(2)
  }
}
