package com.razeware.emitron.ui.download

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.*
import com.razeware.emitron.data.download.DownloadRepository
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.DownloadProgress
import com.razeware.emitron.model.DownloadState
import com.razeware.emitron.ui.content.ContentPagedViewModel
import com.razeware.emitron.ui.login.PermissionActionDelegate
import com.razeware.emitron.ui.onboarding.OnboardingActionDelegate
import com.razeware.emitron.utils.LocalPagedResponse
import com.razeware.emitron.utils.TestCoroutineRule
import com.razeware.emitron.utils.isEqualTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DownloadViewModelTest {

  private val downloadRepository: DownloadRepository = mock()

  private val contentPagedViewModel: ContentPagedViewModel = ContentPagedViewModel()

  private val permissionActionDelegate: PermissionActionDelegate = mock()

  private val onboardingActionDelegate: OnboardingActionDelegate = mock()

  private val downloadActionDelegate: DownloadActionDelegate = mock()

  private lateinit var viewModel: DownloadViewModel

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  @Before
  fun setUp() {
    viewModel = DownloadViewModel(
      downloadRepository,
      contentPagedViewModel,
      onboardingActionDelegate,
      permissionActionDelegate,
      downloadActionDelegate
    )
  }

  @Test
  fun loadDownloads() {
    val localPagedViewModel: LocalPagedResponse<Data> = mock()
    whenever(downloadRepository.getDownloads()).doReturn(localPagedViewModel)
    viewModel.loadDownloads()
    verify(downloadRepository).getDownloads()
    verifyNoMoreInteractions(downloadRepository)
  }


  @Test
  fun getPaginationViewModel() {
    viewModel.getPaginationViewModel() isEqualTo contentPagedViewModel
  }


  @Test
  fun updateDownload() {
    testCoroutineRule.runBlockingTest {
      val downloadProgress = DownloadProgress(
        "1",
        percentDownloaded = 50,
        state = DownloadState.COMPLETED
      )
      viewModel.updateDownload(downloadProgress)
      verify(downloadActionDelegate).updateDownloadProgress(downloadProgress)
      verifyNoMoreInteractions(downloadActionDelegate)
    }
  }
}
