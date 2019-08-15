package com.raywenderlich.emitron.ui.library

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import com.raywenderlich.emitron.data.content.ContentRepository
import com.raywenderlich.emitron.model.Contents
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.ui.content.ContentPagedViewModel
import com.raywenderlich.emitron.ui.utils.observeForTestingResult
import com.raywenderlich.emitron.utils.PagedResponse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyList

class LibraryViewModelTest {

  private val contentRepository: ContentRepository = mock()

  private val contentViewModel: ContentPagedViewModel = ContentPagedViewModel()

  private lateinit var viewModel: LibraryViewModel

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @Before
  fun setUp() {
    viewModel = LibraryViewModel(contentRepository, contentViewModel)
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

}
