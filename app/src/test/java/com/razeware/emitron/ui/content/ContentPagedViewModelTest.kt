package com.razeware.emitron.ui.content

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.razeware.emitron.model.Contents
import com.razeware.emitron.model.Data
import com.razeware.emitron.utils.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ContentPagedViewModelTest {


  private lateinit var viewModel: ContentPagedViewModel

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @Before
  fun setUp() {
    viewModel = ContentPagedViewModel()
  }

  @Test
  fun getNetworkState() {
    val pagedResponse: PagedResponse<Contents, Data> = mock()
    val networkState =
      MutableLiveData<NetworkState>().apply {
        this.value = NetworkState.RUNNING
      }
    whenever(pagedResponse.networkState).doReturn(networkState)
    viewModel.repoResult.postValue(pagedResponse)
    assertThat(viewModel.networkState.observeForTestingResult()).isEqualTo(NetworkState.RUNNING)
  }

  @Test
  fun getContents() {
    val pagedResponse: PagedResponse<Contents, Data> = mock()
    val contents = Contents()
    val initialData =
      MutableLiveData<Contents>().apply { this.value = contents }
    whenever(pagedResponse.initialData).doReturn(initialData)
    viewModel.repoResult.postValue(pagedResponse)
    assertThat(viewModel.contents.observeForTestingResult()).isEqualTo(contents)
  }

  @Test
  fun getContentPagedList() {
    val pagedResponse: PagedResponse<Contents, Data> = mock()
    val pagedList: PagedList<Data> = mock()
    val pagedListData =
      MutableLiveData<PagedList<Data>>().apply { this.value = pagedList }
    whenever(pagedResponse.pagedList).doReturn(pagedListData)
    viewModel.repoResult.postValue(pagedResponse)
    assertThat(viewModel.contentPagedList.observeForTestingResult()).isEqualTo(pagedList)
  }

  @Test
  fun handleItemRetry() {
    val pagedResponse: PagedResponse<Contents, Data> = mock()
    val retry: () -> Unit = mock()
    whenever(pagedResponse.retry).doReturn(retry)
    viewModel.repoResult.postValue(pagedResponse)
    viewModel.handleItemRetry(false)
    val uiStateObserver = viewModel.uiState.observeForTestingObserver()
    verify(uiStateObserver).onChanged(UiStateManager.UiState.ERROR_CONNECTION)
    viewModel.handleItemRetry(true)
    verify(retry).invoke()
  }
}
