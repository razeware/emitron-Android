package com.razeware.emitron.data.filter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.*
import com.razeware.emitron.model.Attributes
import com.razeware.emitron.model.Contents
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.entity.Category
import com.razeware.emitron.model.entity.Domain
import com.razeware.emitron.utils.CurrentThreadExecutor
import com.razeware.emitron.utils.TestCoroutineRule
import com.razeware.emitron.utils.async.ThreadManager
import com.razeware.emitron.utils.isEqualTo
import com.razeware.emitron.utils.observeForTestingResultNullable
import kotlinx.coroutines.Dispatchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FilterRepositoryTest {

  private lateinit var repository: FilterRepository

  private val filterApi: FilterApi = mock()

  private val filterDataSourceLocal: FilterDataSourceLocal = mock()

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  private val threadManager: ThreadManager = mock()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  @Before
  fun setUp() {
    whenever(threadManager.io).doReturn(Dispatchers.Unconfined)
    whenever(threadManager.networkExecutor).doReturn(CurrentThreadExecutor())
    repository = FilterRepository(filterApi, filterDataSourceLocal, threadManager)
  }

  @Test
  fun getCategories() {

    // Given
    whenever(filterDataSourceLocal.getCategories()).doReturn(MutableLiveData<List<Category>>().apply {
      value = listOf(
        Category(categoryId = "1", name = "Architecture"),
        Category(categoryId = "2", name = "Algorithms")
      )
    })

    // When
    val result = repository.getCategories().observeForTestingResultNullable()

    // Then
    result isEqualTo listOf(
      Data(
        id = "1",
        type = "categories",
        attributes = Attributes(name = "Architecture")
      ),
      Data(
        id = "2",
        type = "categories",
        attributes = Attributes(name = "Algorithms")
      )
    )

    verify(filterDataSourceLocal).getCategories()
    verifyNoMoreInteractions(filterDataSourceLocal)
  }

  @Test
  fun fetchCategories() {
    testCoroutineRule.runBlockingTest {
      val response = Contents(
        datum = listOf(Data("1"), Data("2"), Data("3"))
      )
      // Given
      whenever(filterApi.getCategories()).doReturn(response)

      // When
      repository.fetchCategories()

      // Then
      verify(filterApi).getCategories()
      verify(filterDataSourceLocal).saveCategories(
        listOf(Data("1"), Data("2"), Data("3"))
      )
      verifyNoMoreInteractions(filterApi)
      verifyNoMoreInteractions(filterDataSourceLocal)
    }
  }

  @Test
  fun fetchDomains() {
    testCoroutineRule.runBlockingTest {
      val response = Contents(
        datum = listOf(Data("5"), Data("6"), Data("7"))
      )
      // Given
      whenever(filterApi.getDomains()).doReturn(response)

      // When
      repository.fetchDomains()

      // Then
      verify(filterApi).getDomains()
      verify(filterDataSourceLocal).saveDomains(
        listOf(Data("5"), Data("6"), Data("7"))
      )
      verifyNoMoreInteractions(filterApi)
      verifyNoMoreInteractions(filterDataSourceLocal)
    }
  }

  @Test
  fun fetchDomainsAndCategories() {
    testCoroutineRule.runBlockingTest {
      val categoryResponse = Contents(
        datum = listOf(Data("5"), Data("6"), Data("7"))
      )
      // Given
      whenever(filterApi.getCategories()).doReturn(categoryResponse)
      val domainResponse = Contents(
        datum = listOf(Data("5"), Data("6"), Data("7"))
      )
      // Given
      whenever(filterApi.getDomains()).doReturn(domainResponse)

      // When
      repository.fetchDomainsAndCategories()

      // Then
      verify(filterApi).getCategories()
      verify(filterDataSourceLocal).saveCategories(
        listOf(Data("5"), Data("6"), Data("7"))
      )
      verify(filterApi).getDomains()
      verify(filterDataSourceLocal).saveDomains(
        listOf(Data("5"), Data("6"), Data("7"))
      )
      verifyNoMoreInteractions(filterApi)
      verifyNoMoreInteractions(filterDataSourceLocal)
    }
  }

  @Test
  fun getDomains() {
    whenever(filterDataSourceLocal.getDomains()).doReturn(MutableLiveData<List<Domain>>().apply {
      value = listOf(
        Domain(domainId = "1", name = "iOS & Swift"),
        Domain(domainId = "2", name = "Android & Kotlin"),
        Domain(domainId = "1", name = "Flutter & Dart", level = "archived")
      )
    })
    val result = repository.getDomains().observeForTestingResultNullable()
    result isEqualTo listOf(
      Data(
        id = "1",
        type = "domains",
        attributes = Attributes(name = "iOS & Swift", level = null)
      ),
      Data(
        id = "2",
        type = "domains",
        attributes = Attributes(name = "Android & Kotlin", level = null)
      )
    )

    verify(filterDataSourceLocal).getDomains()
    verifyNoMoreInteractions(filterDataSourceLocal)
  }
}
