package com.raywenderlich.emitron.data.filter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.*
import com.raywenderlich.emitron.model.Contents
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.utils.CurrentThreadExecutor
import com.raywenderlich.emitron.utils.TestCoroutineRule
import com.raywenderlich.emitron.utils.async.ThreadManager
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
    repository.getCategories()

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
    repository.getDomains()

    verify(filterDataSourceLocal).getDomains()
    verifyNoMoreInteractions(filterDataSourceLocal)
  }
}
