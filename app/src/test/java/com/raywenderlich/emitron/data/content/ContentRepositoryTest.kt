package com.raywenderlich.emitron.data.content

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import com.raywenderlich.emitron.model.Contents
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.model.Links
import com.raywenderlich.emitron.utils.*
import com.raywenderlich.emitron.utils.async.ThreadManager
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito
import retrofit2.Response
import retrofit2.mock.Calls

class ContentRepositoryTest {

  private lateinit var repository: ContentRepository

  private val contentApi: ContentApi = mock()

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  private val threadManager: ThreadManager = mock()

  @Before
  fun setUp() {
    whenever(threadManager.networkIo).doReturn(CurrentThreadExecutor())
    repository = ContentRepository(contentApi, threadManager)
  }

  /**
   * Test no data returned from API
   */
  @Test
  fun getContents_noData() {

    // Given
    val contents = Contents(datum = emptyList())
    whenever(
      contentApi.getContents(
        pageNumber = anyInt(),
        pageSize = anyInt(),
        contentType = anyList(),
        category = eq(emptyList()),
        domain = eq(emptyList())
      )
    ).doReturn(
      Calls.response(
        contents
      )
    )

    // When
    val result = repository.getContents(filters = emptyList(), pageSize = 5)
    val networkObserver = result.networkState?.observeForTestingObserver()
    val pagedList = result.pagedList.observeForTestingResult()
    val response: Contents? = result.initialData.observeForTestingResultNullable()

    // Then
    assertThat(pagedList).isNotNull()
    assertThat(pagedList.size).isEqualTo(0)
    assertThat(result.networkState.observeForTestingResult()).isEqualTo(NetworkState.INIT_EMPTY)
    assertThat(response).isNull()

    networkObserver?.let {
      val inOrder = Mockito.inOrder(networkObserver)
      inOrder.verify(networkObserver).onChanged(NetworkState.INIT)
      inOrder.verify(networkObserver).onChanged(NetworkState.INIT_EMPTY)
      inOrder.verifyNoMoreInteractions()
    }
  }

  /**
   * Test correct API parameters are passed
   */
  @Test
  fun getContents_correctApiParam() {

    // Given
    val contents = Contents(datum = emptyList())
    whenever(
      contentApi.getContents(
        pageNumber = anyInt(),
        pageSize = anyInt(),
        contentType = anyList(),
        category = eq(emptyList()),
        domain = eq(emptyList())
      )
    ).doReturn(
      Calls.response(
        contents
      )
    )

    // When
    val result = repository.getContents(filters = emptyList(), pageSize = 5)
    result.pagedList.observeForTestingResult()

    // Then
    val expectedContentTypeFilter = listOf("collection", "screencast")
    verify(contentApi).getContents(
      1,
      5,
      expectedContentTypeFilter,
      emptyList(),
      emptyList()
    )
    verifyNoMoreInteractions(contentApi)
  }

  /**
   * Test single item load
   */
  @Test
  fun getContents_someData() {

    // Given
    val data = Data()
    val contents = Contents(datum = listOf(data))
    whenever(
      contentApi.getContents(
        pageNumber = anyInt(),
        pageSize = anyInt(),
        contentType = anyList(),
        category = eq(emptyList()),
        domain = eq(emptyList())
      )
    ).doReturn(
      Calls.response(contents)
    )

    // When
    val result = repository.getContents(filters = emptyList(), pageSize = 5)

    // Then
    assertThat(result.pagedList.observeForTestingResult()).isEqualTo(listOf(data))
  }

  /**
   * Test complete data load
   */
  @Test
  fun getContents_completeData() {

    // Given
    val data = (1..5).map { Data(id = it.toString()) }
    val links: Links = mock()
    whenever(links.getNextPage()).doReturn(2)
    val contents = Contents(datum = data, links = links)
    whenever(
      contentApi.getContents(
        pageNumber = anyInt(),
        pageSize = anyInt(),
        contentType = anyList(),
        category = eq(emptyList()),
        domain = eq(emptyList())
      )
    ).doReturn(
      Calls.response(contents)
    )

    // When
    val result = repository.getContents(emptyList(), 5)
    val pagedList = result.pagedList.observeForTestingResult()

    // Then
    assertThat(pagedList).isEqualTo(data)

    // Load next page
    // Given
    whenever(links.getNextPage()).doReturn(null)
    whenever(
      contentApi.getContents(
        pageNumber = anyInt(),
        pageSize = anyInt(),
        contentType = anyList(),
        category = eq(emptyList()),
        domain = eq(emptyList())
      )
    ).doReturn(
      Calls.response(contents)
    )

    // When
    pagedList.loadAllData()

    // Then
    assertThat(pagedList).isNotNull()
    assertThat(pagedList.size).isEqualTo(10)
  }

  /**
   * Test initial load error
   */
  @Test
  fun getContents_failure() {

    // Given
    val responseBody: ResponseBody = mock()
    whenever(
      contentApi.getContents(
        pageNumber = anyInt(),
        pageSize = anyInt(),
        contentType = anyList(),
        category = eq(emptyList()),
        domain = eq(emptyList())
      )
    ).doReturn(
      Calls.response(Response.error(500, responseBody))
    )

    // When
    val result = repository.getContents(emptyList(), 5)
    result.pagedList.observeForTestingResult()

    // Then
    assertThat(result.networkState?.observeForTestingResult())
      .isEqualTo(NetworkState.INIT_FAILED)
  }

  /**
   * Test retry after API error on first page load
   */
  @Test
  fun getContents_retryOn_failure() {

    // Given
    val responseBody: ResponseBody = mock()
    whenever(
      contentApi.getContents(
        pageNumber = anyInt(),
        pageSize = anyInt(),
        contentType = anyList(),
        category = eq(emptyList()),
        domain = eq(emptyList())
      )
    ).doReturn(
      Calls.response(Response.error(500, responseBody))
    )

    // When
    val result = repository.getContents(emptyList(), 5)
    val networkObserver = result.networkState?.observeForTestingObserver()
    val list = result.pagedList.observeForTestingResult()

    // Then
    assertThat(result.networkState?.observeForTestingResult()).isEqualTo(NetworkState.INIT_FAILED)

    // Given
    val data = (1..2).map { Data(id = it.toString()) }
    val contents = Contents(datum = data)
    whenever(
      contentApi.getContents(
        pageNumber = anyInt(),
        pageSize = anyInt(),
        contentType = anyList(),
        category = eq(emptyList()),
        domain = eq(emptyList())
      )
    ).doReturn(Calls.response(contents))

    // When
    result.retry?.invoke()

    // Then
    assertThat(list.size).isEqualTo(2)
    assertThat(result.networkState?.observeForTestingResult()).isEqualTo(NetworkState.SUCCESS)

    networkObserver?.let {
      val inOrder = Mockito.inOrder(networkObserver)
      inOrder.verify(networkObserver).onChanged(NetworkState.INIT)
      inOrder.verify(networkObserver).onChanged(NetworkState.INIT_FAILED)
      inOrder.verify(networkObserver).onChanged(NetworkState.INIT)
      inOrder.verify(networkObserver).onChanged(NetworkState.SUCCESS)
      inOrder.verifyNoMoreInteractions()
    }
  }

  /**
   * Test retry when first page loads, but subsequent page load fails due to API error
   */
  @Test
  fun retryAfterInitialFails() {

    // Given
    val data = (1..10).map { Data(id = it.toString()) }
    val links: Links = mock()
    whenever(links.getNextPage()).doReturn(2)
    whenever(
      contentApi.getContents(
        pageNumber = anyInt(),
        pageSize = anyInt(),
        contentType = anyList(),
        category = eq(emptyList()),
        domain = eq(emptyList())
      )
    ).doReturn(
      Calls.response(
        Contents(
          data.subList(0, 5),
          links = links
        )
      )
    )
    // When
    val result = repository.getContents(emptyList(), 5)
    val networkObserver = result.networkState?.observeForTestingObserver()
    val list = result.pagedList.observeForTestingResult()

    // Then
    assertThat(list.size < data.size).isTrue()
    assertThat(result.networkState?.observeForTestingResult()).isEqualTo(NetworkState.SUCCESS)

    // Given
    val responseBody: ResponseBody = mock()
    whenever(
      contentApi.getContents(
        pageNumber = anyInt(),
        pageSize = anyInt(),
        contentType = anyList(),
        category = eq(emptyList()),
        domain = eq(emptyList())
      )
    ).doReturn(
      Calls.response(Response.error(500, responseBody))
    )
    // When
    list.loadAllData()

    // Then
    assertThat(result.networkState?.observeForTestingResult()).isEqualTo(NetworkState.FAILED)
    assertThat(result.retry).isNotNull()

    // Given
    val contents = Contents(datum = data.subList(5, 10))
    whenever(
      contentApi.getContents(
        pageNumber = anyInt(),
        pageSize = anyInt(),
        contentType = anyList(),
        category = eq(emptyList()),
        domain = eq(emptyList())
      )
    ).doReturn(Calls.response(contents))

    // When
    result.retry?.invoke()

    // Then
    assertThat(result.networkState?.observeForTestingResult()).isEqualTo(NetworkState.SUCCESS)
    assertThat(list).isEqualTo(data)

    networkObserver?.let {
      val inOrder = Mockito.inOrder(networkObserver)
      inOrder.verify(networkObserver).onChanged(NetworkState.INIT)
      inOrder.verify(networkObserver).onChanged(NetworkState.SUCCESS)
      inOrder.verify(networkObserver).onChanged(NetworkState.FAILED)
      inOrder.verify(networkObserver).onChanged(NetworkState.RUNNING)
      inOrder.verify(networkObserver).onChanged(NetworkState.SUCCESS)
      inOrder.verifyNoMoreInteractions()
    }
  }
}
