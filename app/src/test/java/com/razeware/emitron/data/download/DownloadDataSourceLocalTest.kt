package com.razeware.emitron.data.download

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.razeware.emitron.data.content.dao.DownloadDao
import com.razeware.emitron.model.DownloadProgress
import com.razeware.emitron.model.DownloadState
import com.razeware.emitron.model.entity.Download
import com.razeware.emitron.utils.TestCoroutineRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Month
import org.threeten.bp.format.DateTimeFormatter

class DownloadDataSourceLocalTest {

  private val downloadDao: DownloadDao = mock()

  private lateinit var downloadDataSourceLocal: DownloadDataSourceLocal

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  @Before
  fun setUp() {
    downloadDataSourceLocal =
      DownloadDataSourceLocal(downloadDao)
  }

  @Test
  fun updateDownloadUrl() {
    testCoroutineRule.runBlockingTest {
      downloadDataSourceLocal.updateDownloadUrl("1", "download/1")
      verify(downloadDao).updateUrl("1", "download/1", 2)
      verifyNoMoreInteractions(downloadDao)
    }
  }

  @Test
  fun updateDownloadProgress() {
    testCoroutineRule.runBlockingTest {
      val downloadProgress = DownloadProgress(
        "1",
        25,
        DownloadState.COMPLETED
      )
      downloadDataSourceLocal.updateDownloadProgress(downloadProgress)
      verify(downloadDao).updateProgress("1", 25, 3)
      verifyNoMoreInteractions(downloadDao)
    }
  }

  @Test
  fun updateDownloadState() {
    testCoroutineRule.runBlockingTest {
      downloadDataSourceLocal.updateDownloadState(
        listOf("1"),
        DownloadState.COMPLETED
      )
      verify(downloadDao).updateState(listOf("1"), 3)
      verifyNoMoreInteractions(downloadDao)
    }
  }

  @Test
  fun insertDownload() {
    testCoroutineRule.runBlockingTest {
      val today = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)
      val download = Download(
        "1",
        state = 3,
        createdAt = today.format(DateTimeFormatter.ISO_DATE_TIME)
      )
      downloadDataSourceLocal.insertDownload("1", DownloadState.COMPLETED, today)
      verify(downloadDao).insert(download)
      verifyNoMoreInteractions(downloadDao)
    }
  }

  @Test
  fun deleteDownload() {
    testCoroutineRule.runBlockingTest {
      downloadDataSourceLocal.deleteDownload(listOf("1"))
      verify(downloadDao).delete(
        listOf("1")
      )
      verifyNoMoreInteractions(downloadDao)
    }
  }

  @Test
  fun deleteAllDownloads() {
    testCoroutineRule.runBlockingTest {
      downloadDataSourceLocal.deleteAllDownloads()
      verify(downloadDao).deleteAll()
      verifyNoMoreInteractions(downloadDao)
    }
  }

  @Test
  fun getQueuedDownloads_A() {
    testCoroutineRule.runBlockingTest {
      downloadDataSourceLocal.getQueuedDownloads(
        1,
        arrayOf(1, 2),
        arrayOf("screencast", "episode")
      )
      verify(downloadDao).getQueuedDownloads(1, arrayOf(1, 2), arrayOf("screencast", "episode"))
      verifyNoMoreInteractions(downloadDao)
    }
  }

  @Test
  fun getQueuedDownload() {
    testCoroutineRule.runBlockingTest {
      downloadDataSourceLocal.getDownload("1")
      verify(downloadDao).getDownload("1")
      verifyNoMoreInteractions(downloadDao)
    }
  }

  @Test
  fun getQueuedDownloads_B() {
    testCoroutineRule.runBlockingTest {
      downloadDataSourceLocal.getQueuedDownloads()
      verify(downloadDao).getQueuedDownloads(arrayOf("collection", "screencast"))
      verifyNoMoreInteractions(downloadDao)
    }
  }

  @Test
  fun getDownloadsById() {
    testCoroutineRule.runBlockingTest {
      val downloadIds = listOf("1", "2", "3")
      downloadDataSourceLocal.getDownloadsById(downloadIds)
      verify(downloadDao).getDownloadsById(downloadIds)
      verifyNoMoreInteractions(downloadDao)
    }
  }

  @Test
  fun getInProgressDownloads() {
    testCoroutineRule.runBlockingTest {
      downloadDataSourceLocal.getInProgressDownloads(arrayOf("screencast", "episode"))
      verify(downloadDao).getInProgressDownloads(2, arrayOf("screencast", "episode"))
      verifyNoMoreInteractions(downloadDao)
    }
  }
}
