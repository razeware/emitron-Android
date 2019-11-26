package com.razeware.emitron.ui.library

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import com.razeware.emitron.data.content.ContentRepository
import com.razeware.emitron.data.filter.FilterRepository
import com.razeware.emitron.model.Contents
import com.razeware.emitron.model.Data
import com.razeware.emitron.ui.content.ContentPagedViewModel
import com.razeware.emitron.utils.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyList

class LibraryViewModelTest {

  private val contentRepository: ContentRepository = mock()
  private val filterRepository: FilterRepository = mock()
  private val logger: LoggerImpl = mock()

  private val contentViewModel: ContentPagedViewModel = ContentPagedViewModel()

  private lateinit var viewModel: LibraryViewModel

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  @Before
  fun setUp() {
    viewModel = LibraryViewModel(contentRepository, contentViewModel, filterRepository, logger)
  }

  @Test
  fun loadCollections() {

    // Given
    val response: PagedResponse<Contents, Data> = mock()
    whenever(contentRepository.getContents(anyList(), anyInt())).doReturn(response)

    // When
    viewModel.loadCollections(emptyList())

    // Then
    verify(contentRepository).getContents(emptyList(), 10)
    verifyNoMoreInteractions(contentRepository)

    contentViewModel.repoResult.observeForTestingResult()
    assertThat(contentViewModel.repoResult.value).isEqualTo(response)
  }

  @Test
  fun loadSearchQueries() {
    // Given
    whenever(contentRepository.getSearchQueries()).doReturn(listOf("Emitron", "Android"))

    // When
    val result = viewModel.loadSearchQueries()

    // Then
    result isEqualTo listOf("Emitron", "Android")
    verify(contentRepository).getSearchQueries()
    verifyNoMoreInteractions(contentRepository)
  }

  @Test
  fun saveSearchQuery() {
    // When
    viewModel.saveSearchQuery("Emitron")

    // Then
    verify(contentRepository).saveSearchQuery("Emitron")
    verifyNoMoreInteractions(contentRepository)
  }

  @Test
  fun syncDomainsAndCategories() {
    testCoroutineRule.runBlockingTest {

      // When
      viewModel.syncDomainsAndCategories()

      // Then
      verify(filterRepository).fetchDomainsAndCategories()
      verifyNoMoreInteractions(filterRepository)
    }
  }
}
