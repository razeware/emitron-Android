package com.razeware.emitron.data.progressions

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.razeware.emitron.data.content.dao.ContentDao
import com.razeware.emitron.data.progressions.dao.ProgressionDao
import com.razeware.emitron.data.progressions.dao.WatchStatDao
import com.razeware.emitron.model.entity.WatchStat
import com.razeware.emitron.utils.TestCoroutineRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Month

class ProgressionDataSourceLocalTest {

  private val progressionDao: ProgressionDao = mock()

  private val watchStatDao: WatchStatDao = mock()

  private val contentDao: ContentDao = mock()

  private lateinit var progressionDataSourceLocal: ProgressionDataSourceLocal

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  @Before
  fun setUp() {
    progressionDataSourceLocal =
      ProgressionDataSourceLocal(progressionDao, contentDao, watchStatDao)
  }

  @Test
  fun updateProgress() {
    testCoroutineRule.runBlockingTest {
      val today = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)

      progressionDataSourceLocal.updateProgress(
        contentId = "1",
        percentComplete = 1,
        progress = 50,
        finished = true,
        synced = true,
        updatedAt = today,
        progressionId = "1"
      )
      verify(progressionDao).insertOrUpdateProgress(
        "1",
        finished = true,
        synced = true,
        percentComplete = 1,
        progress = 50,
        updatedAt = "2019-08-11T02:00:00",
        progressionId = "1",
        contentDao = contentDao
      )
      verifyNoMoreInteractions(progressionDao)
    }
  }

  @Test
  fun deleteWatchStats() {
    testCoroutineRule.runBlockingTest {
      progressionDataSourceLocal.deleteWatchStats()
      verify(watchStatDao).deleteAll()
      verifyNoMoreInteractions(watchStatDao)
    }
  }

  @Test
  fun getWatchStats() {
    testCoroutineRule.runBlockingTest {
      progressionDataSourceLocal.getWatchStats()
      verify(watchStatDao).getAll()
      verifyNoMoreInteractions(watchStatDao)
    }
  }

  @Test
  fun updateWatchStat() {
    testCoroutineRule.runBlockingTest {
      val today = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)

      progressionDataSourceLocal.updateWatchStat(
        contentId = "1",
        duration = 50,
        watchedAt = today
      )
      verify(watchStatDao).insertOrUpdateWatchStat(
        WatchStat(
          "1", 50, "2019081102", "2019-08-11T02:00:00"
        )
      )
      verifyNoMoreInteractions(watchStatDao)
    }
  }
}
