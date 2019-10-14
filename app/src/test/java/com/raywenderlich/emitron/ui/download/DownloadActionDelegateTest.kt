package com.raywenderlich.emitron.ui.download

import com.nhaarman.mockitokotlin2.*
import com.raywenderlich.emitron.data.*
import com.raywenderlich.emitron.data.download.DownloadRepository
import com.raywenderlich.emitron.data.login.LoginRepository
import com.raywenderlich.emitron.model.Download
import com.raywenderlich.emitron.model.DownloadState
import com.raywenderlich.emitron.utils.TestCoroutineRule
import com.raywenderlich.emitron.utils.isEqualTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DownloadActionDelegateTest {

  private val downloadRepository: DownloadRepository = mock()
  private val loginRepository: LoginRepository = mock()

  private lateinit var viewModel: DownloadActionDelegate

  @Before
  fun setUp() {
    viewModel = DownloadActionDelegate(downloadRepository, loginRepository)
  }

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  @Test
  fun getDownloads() {
    testCoroutineRule.runBlockingTest {
      val downloadIds = listOf("1", "2")

      viewModel.getDownloads(downloadIds)
      verify(downloadRepository).getDownloadsById(downloadIds)
      verifyNoMoreInteractions(downloadRepository)
    }
  }

  @Test
  fun updateDownloadProgress() {
    testCoroutineRule.runBlockingTest {

      // When
      viewModel.updateDownloadProgress("1", 25, DownloadState.COMPLETED)

      verify(downloadRepository).updateDownloadProgress(
        "1",
        25,
        DownloadState.COMPLETED
      )
      verifyNoMoreInteractions(downloadRepository)
    }
  }

  @Test
  fun getCollectionDownloadState_Screencast() {
    val contentData = buildContentData(
      withRelationship(
        withRelatedBookmark(),
        withRelatedDomains(),
        groups = withGroups(withGroupContents())
      ),
      contentType = "screencast"
    )

    val result = viewModel.getCollectionDownloadState(
      contentData,
      listOf(
        createDownload()
      )
    )

    // Then
    result isEqualTo Download(
      progress = 25,
      state = 3,
      failureReason = 0,
      url = "download/1"
    )
    verifyNoMoreInteractions(downloadRepository)
  }

  @Test
  fun getCollectionDownloadState_Collection() {
    // Given
    val contentData =
      buildContentData(
        withRelationship(
          withRelatedBookmark(),
          withRelatedDomains(),
          groups = withGroups(withGroupContents())
        ),
        contentType = "collection"
      )

    // When
    val result = viewModel.getCollectionDownloadState(
      contentData,
      listOf(createDownload())
    )

    // Then
    result isEqualTo Download(
      progress = 100,
      state = 3,
      failureReason = 0,
      url = null
    )
    verifyNoMoreInteractions(downloadRepository)
  }

  @Test
  fun isDownloadAllowed() {
    whenever(loginRepository.hasDownloadPermission()).doReturn(true)

    val result = viewModel.isDownloadAllowed()

    result isEqualTo true
    verify(loginRepository).hasDownloadPermission()
    verifyNoMoreInteractions(loginRepository)
  }
}
