package com.razeware.emitron.data.content

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import com.raywenderlich.android.preferences.GeneralSettingsPrefs
import com.razeware.emitron.model.*
import com.razeware.emitron.utils.*
import com.razeware.emitron.utils.async.ThreadManager
import kotlinx.coroutines.Dispatchers
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito
import retrofit2.Response
import retrofit2.mock.Calls

class ContentRepositoryTest {

  private lateinit var repository: ContentRepository

  private val contentApi: ContentApi = mock()
  private val contentDataSourceLocal: ContentDataSourceLocal = mock()

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  private val threadManager: ThreadManager = mock()

  private val settingsPref: GeneralSettingsPrefs = mock()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  @Before
  fun setUp() {
    whenever(threadManager.io).doReturn(Dispatchers.Unconfined)
    whenever(threadManager.networkExecutor).doReturn(CurrentThreadExecutor())
    repository = ContentRepository(contentApi, threadManager, settingsPref, contentDataSourceLocal)
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
        category = anyList(),
        difficulty = anyList(),
        domain = anyList(),
        search = anyString(),
        sort = anyString(),
        professional = anyOrNull()
      )
    ).doReturn(
      Calls.response(
        contents
      )
    )

    // When
    val result = repository.getContents(filters = emptyList(), pageSize = 5)
    val networkObserver = result.uiState?.observeForTestingObserver()
    val pagedList = result.pagedList.observeForTestingResult()
    val response: Contents? = result.initialData.observeForTestingResultNullable()

    // Then
    assertThat(pagedList).isNotNull()
    assertThat(pagedList.size).isEqualTo(0)
    assertThat(result.uiState.observeForTestingResult()).isEqualTo(UiStateManager.UiState.INIT_EMPTY)
    assertThat(response).isNull()

    networkObserver?.let {
      val inOrder = Mockito.inOrder(networkObserver)
      inOrder.verify(networkObserver).onChanged(UiStateManager.UiState.INIT)
      inOrder.verify(networkObserver).onChanged(UiStateManager.UiState.INIT_EMPTY)
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
        category = anyList(),
        difficulty = anyList(),
        domain = anyList(),
        search = anyString(),
        sort = anyString(),
        professional = anyBoolean()
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
      emptyList(),
      emptyList(),
      "",
      "-released_at",
      null
    )
    verifyNoMoreInteractions(contentApi)
  }

  /**
   * Test correct API filters are passed
   */
  @Test
  fun getContents_correctApiParamWithFilters() {

    // Given
    val contents = Contents(datum = emptyList())
    whenever(
      contentApi.getContents(
        pageNumber = anyInt(),
        pageSize = anyInt(),
        contentType = anyList(),
        category = anyList(),
        difficulty = anyList(),
        domain = anyList(),
        search = anyString(),
        sort = anyString(),
        professional = anyBoolean()
      )
    ).doReturn(
      Calls.response(
        contents
      )
    )

    val filterList = listOf(
      Data(id = "1", type = DataType.Categories.toRequestFormat()),
      Data(type = FilterType.Search.toRequestFormat(), attributes = Attributes(name = "Emitron")),
      Data(id = "2", type = DataType.Domains.toRequestFormat()),
      Data(type = FilterType.Sort.toRequestFormat(), attributes = Attributes(name = "popularity"))
    )

    // When
    val result =
      repository.getContents(filters = filterList, pageSize = 5)
    result.pagedList.observeForTestingResult()

    // Then
    val expectedContentTypeFilter = listOf("collection", "screencast")
    verify(contentApi).getContents(
      1,
      5,
      expectedContentTypeFilter,
      listOf("1"),
      listOf("2"),
      emptyList(),
      "Emitron",
      "-popularity",
      null
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
        category = anyList(),
        difficulty = anyList(),
        domain = anyList(),
        search = anyString(),
        sort = anyString(),
        professional = anyOrNull()
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
        category = anyList(),
        difficulty = anyList(),
        domain = anyList(),
        search = anyString(),
        sort = anyString(),
        professional = anyOrNull()
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
        difficulty = anyList(),
        category = anyList(),
        domain = anyList(),
        search = anyString(),
        sort = anyString(),
        professional = anyOrNull()
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
    val response = Calls.response<Contents>(Response.error(500, responseBody))
    whenever(
      contentApi.getContents(
        pageNumber = anyInt(),
        pageSize = anyInt(),
        contentType = anyList(),
        category = anyList(),
        difficulty = anyList(),
        domain = anyList(),
        search = anyString(),
        sort = anyString(),
        professional = anyBoolean()
      )
    ).doReturn(response)

    // When
    val result = repository.getContents(emptyList(), 5)
    result.pagedList.observeForTestingResult()

    // Then
    assertThat(result.uiState?.observeForTestingResult())
      .isEqualTo(UiStateManager.UiState.INIT_FAILED)
  }

  /**
   * Test retry after API error on first page load
   */
  @Test
  fun getContents_retryOn_failure() {

    // Given
    val responseBody: ResponseBody = mock()
    val response = Calls.response<Contents>(Response.error(500, responseBody))
    whenever(
      contentApi.getContents(
        pageNumber = anyInt(),
        pageSize = anyInt(),
        contentType = anyList(),
        category = anyList(),
        difficulty = anyList(),
        domain = anyList(),
        search = anyString(),
        sort = anyString(),
        professional = anyBoolean()
      )
    ).doReturn(response)

    // When
    val result = repository.getContents(emptyList(), 5)
    val networkObserver = result.uiState?.observeForTestingObserver()
    val list = result.pagedList.observeForTestingResult()

    // Then
    assertThat(result.uiState?.observeForTestingResult()).isEqualTo(UiStateManager.UiState.INIT_FAILED)

    // Given
    val data = (1..2).map { Data(id = it.toString()) }
    val contents = Contents(datum = data)
    whenever(
      contentApi.getContents(
        pageNumber = anyInt(),
        pageSize = anyInt(),
        contentType = anyList(),
        difficulty = anyList(),
        category = anyList(),
        domain = anyList(),
        search = anyString(),
        sort = anyString(),
        professional = anyOrNull()
      )
    ).doReturn(Calls.response(contents))

    // When
    result.retry?.invoke()

    // Then
    assertThat(list.size).isEqualTo(2)
    assertThat(result.uiState?.observeForTestingResult()).isEqualTo(
      UiStateManager.UiState.INIT_LOADED
    )

    networkObserver?.let {
      val inOrder = Mockito.inOrder(networkObserver)
      inOrder.verify(networkObserver).onChanged(UiStateManager.UiState.INIT)
      inOrder.verify(networkObserver).onChanged(UiStateManager.UiState.INIT_FAILED)
      inOrder.verify(networkObserver).onChanged(UiStateManager.UiState.INIT)
      inOrder.verify(networkObserver).onChanged(UiStateManager.UiState.INIT_LOADED)
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
        category = anyList(),
        difficulty = anyList(),
        domain = anyList(),
        search = anyString(),
        sort = anyString(),
        professional = anyOrNull()
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
    val networkObserver = result.uiState?.observeForTestingObserver()
    val list = result.pagedList.observeForTestingResult()

    // Then
    assertThat(list.size < data.size).isTrue()
    assertThat(result.uiState?.observeForTestingResult()).isEqualTo(UiStateManager.UiState.INIT_LOADED)

    // Given
    val responseBody: ResponseBody = mock()
    val response = Calls.response<Contents>(Response.error(500, responseBody))
    whenever(
      contentApi.getContents(
        pageNumber = anyInt(),
        pageSize = anyInt(),
        contentType = anyList(),
        difficulty = anyList(),
        category = anyList(),
        domain = anyList(),
        search = anyString(),
        sort = anyString(),
        professional = anyBoolean()
      )
    ).doReturn(response)
    // When
    list.loadAllData()

    // Then
    assertThat(result.uiState?.observeForTestingResult()).isEqualTo(UiStateManager.UiState.ERROR)
    assertThat(result.retry).isNotNull()

    // Given
    val contents = Contents(datum = data.subList(5, 10))
    whenever(
      contentApi.getContents(
        pageNumber = anyInt(),
        pageSize = anyInt(),
        contentType = anyList(),
        difficulty = anyList(),
        category = anyList(),
        domain = anyList(),
        search = anyString(),
        sort = anyString(),
        professional = anyOrNull()
      )
    ).doReturn(Calls.response(contents))

    // When
    result.retry?.invoke()

    // Then
    assertThat(result.uiState?.observeForTestingResult()).isEqualTo(UiStateManager.UiState.LOADED)
    assertThat(list).isEqualTo(data)

    networkObserver?.let {
      val inOrder = Mockito.inOrder(networkObserver)
      inOrder.verify(networkObserver).onChanged(UiStateManager.UiState.INIT)
      inOrder.verify(networkObserver).onChanged(UiStateManager.UiState.INIT_LOADED)
      inOrder.verify(networkObserver).onChanged(UiStateManager.UiState.ERROR)
      inOrder.verify(networkObserver).onChanged(UiStateManager.UiState.LOADING)
      inOrder.verify(networkObserver).onChanged(UiStateManager.UiState.LOADED)
      inOrder.verifyNoMoreInteractions()
    }
  }

  @Test
  fun getContent() {
    testCoroutineRule.runBlockingTest {
      // Given
      val expectedContent = Content()

      // When
      whenever(contentApi.getContent("1")).doReturn(expectedContent)

      // Then
      val result = repository.getContent("1")
      assertThat(result).isEqualTo(expectedContent)

      verify(contentApi).getContent("1")
      verifyNoMoreInteractions(contentApi)
    }
  }

  @Test
  fun getContentFromDb() {
    testCoroutineRule.runBlockingTest {
      // Given
      val expectedContent = com.razeware.emitron.data.createContentWithDownload()

      // When
      whenever(contentDataSourceLocal.getContent("1")).doReturn(
        com.razeware.emitron.data.createContentDetail()
      )

      // Then
      val result = repository.getContentFromDb("1")
      assertThat(result).isEqualTo(expectedContent)

      verify(contentDataSourceLocal).getContent("1")
      verifyNoMoreInteractions(contentApi)
    }
  }

  @Test(expected = Exception::class)
  fun getContent_failure() {
    testCoroutineRule.runBlockingTest {
      // Given
      val expected = RuntimeException()
      whenever(contentApi.getContent(anyString())).doThrow(expected)

      // When
      repository.getContent("1")

      // Then
      verify(contentApi).getContent("1")
      verifyNoMoreInteractions(contentApi)
    }
  }

  @Test
  fun getSearchQuery() {
    // Given
    whenever(settingsPref.getSearchQueries()).doReturn(listOf("Emitron", "Swift"))

    // When
    val result = repository.getSearchQueries()

    // Then
    result isEqualTo listOf("Emitron", "Swift")
    verify(settingsPref).getSearchQueries()
    verifyNoMoreInteractions(settingsPref)
  }

  @Test
  fun saveSearchQuery() {
    // When
    repository.saveSearchQuery("Emitron")

    // Then
    verify(settingsPref).saveSearchQuery("Emitron")
    verifyNoMoreInteractions(settingsPref)
  }
}
