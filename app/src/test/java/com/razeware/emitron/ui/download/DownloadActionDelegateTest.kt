package com.razeware.emitron.ui.download

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.razeware.emitron.data.*
import com.razeware.emitron.data.download.DownloadRepository
import com.razeware.emitron.data.settings.SettingsRepository
import com.razeware.emitron.model.Download
import com.razeware.emitron.model.DownloadProgress
import com.razeware.emitron.model.DownloadState
import com.razeware.emitron.utils.TestCoroutineRule
import com.razeware.emitron.utils.isEqualTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DownloadActionDelegateTest {

  private val downloadRepository: DownloadRepository = mock()

  private val settingsRepository: SettingsRepository = mock()

  private lateinit var viewModel: DownloadActionDelegate

  @Before
  fun setUp() {
    viewModel = DownloadActionDelegate(downloadRepository, settingsRepository)
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
  fun downloadsWifiOnly() {
    viewModel.downloadsWifiOnly()
    verify(settingsRepository).getDownloadsWifiOnly()
    verifyNoMoreInteractions(settingsRepository)
  }

  @Test
  fun updateDownloadProgress() {
    testCoroutineRule.runBlockingTest {

      // Given
      val downloadProgress = DownloadProgress(
        "1",
        25,
        DownloadState.COMPLETED
      )

      // When
      viewModel.updateDownloadProgress(downloadProgress)

      verify(downloadRepository).updateDownloadProgress(downloadProgress)
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
      ),
      downloadIds = listOf("1")
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
      listOf(createDownload()),
      downloadIds = listOf("1")
    )

    // Then
    result isEqualTo Download(
      progress = 100,
      state = 3,
      failureReason = 0,
      url = null,
      cached = true
    )
    verifyNoMoreInteractions(downloadRepository)
  }

  @Test
  fun getCollectionDownloadState_Collection_PartialDownloaded() {
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
      listOf(createDownload(state = DownloadState.IN_PROGRESS.ordinal)),
      downloadIds = listOf("1", "2")
    )

    // Then
    result isEqualTo Download(
      progress = 25,
      state = DownloadState.PAUSED.ordinal,
      failureReason = 0,
      url = null,
      cached = true
    )
    verifyNoMoreInteractions(downloadRepository)
  }
}
