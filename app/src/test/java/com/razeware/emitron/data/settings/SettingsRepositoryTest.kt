package com.razeware.emitron.data.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.*
import com.raywenderlich.android.preferences.GeneralSettingsPrefs
import com.razeware.emitron.data.content.ContentDataSourceLocal
import com.razeware.emitron.data.login.LoginPrefs
import com.razeware.emitron.data.progressions.ProgressionDataSourceLocal
import com.razeware.emitron.ui.onboarding.OnboardingView
import com.razeware.emitron.utils.CurrentThreadExecutor
import com.razeware.emitron.utils.TestCoroutineRule
import com.razeware.emitron.utils.async.ThreadManager
import com.razeware.emitron.utils.isEqualTo
import kotlinx.coroutines.Dispatchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsRepositoryTest {

  private lateinit var settingsRepository: SettingsRepository

  private val settingsPrefs: GeneralSettingsPrefs = mock()

  private val loginPrefs: LoginPrefs = mock()

  private val threadManager: ThreadManager = mock()

  private val contentDataSourceLocal: ContentDataSourceLocal = mock()

  private val progressionDataSourceLocal: ProgressionDataSourceLocal = mock()

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  @Before
  fun setUp() {
    whenever(threadManager.io).doReturn(Dispatchers.Unconfined)
    whenever(threadManager.db).doReturn(Dispatchers.Unconfined)
    whenever(threadManager.networkExecutor).doReturn(CurrentThreadExecutor())
    settingsRepository = SettingsRepository(
      threadManager,
      loginPrefs,
      settingsPrefs,
      contentDataSourceLocal,
      progressionDataSourceLocal
    )
  }

  @Test
  fun updateCrashReportingAllowed() {
    settingsRepository.updateCrashReportingAllowed(true)
    verify(settingsPrefs).saveCrashReportingAllowed(true)
    verifyNoMoreInteractions(settingsPrefs)
  }

  @Test
  fun updateNightMode() {
    settingsRepository.updateNightMode(1)
    verify(settingsPrefs).saveNightMode(1)
    verifyNoMoreInteractions(settingsPrefs)
  }

  @Test
  fun getNightMode() {
    whenever(settingsPrefs.getNightMode()).doReturn(1)
    val result = settingsRepository.getNightMode()
    result isEqualTo 1
    verify(settingsPrefs).getNightMode()
    verifyNoMoreInteractions(settingsPrefs)
  }

  @Test
  fun isCrashReportingAllowed() {
    whenever(settingsPrefs.isCrashReportingAllowed()).doReturn(true)
    val result = settingsRepository.isCrashReportingAllowed()
    result isEqualTo true
    verify(settingsPrefs).isCrashReportingAllowed()
    verifyNoMoreInteractions(settingsPrefs)
  }

  @Test
  fun updateAutoPlaybackAllowed() {
    settingsRepository.updateAutoPlaybackAllowed(true)
    verify(settingsPrefs).saveAutoPlayAllowed(true)
    verifyNoMoreInteractions(settingsPrefs)
  }

  @Test
  fun updateSubtitleLanguage() {
    settingsRepository.updateSubtitleLanguage("en")
    verify(settingsPrefs).saveSubtitleLanguage("en")
    verifyNoMoreInteractions(settingsPrefs)
  }

  @Test
  fun updateSelectedPlaybackSpeed() {
    settingsRepository.updateSelectedPlaybackSpeed(1.0f)
    verify(settingsPrefs).savePlaybackSpeed(1.0f)
    verifyNoMoreInteractions(settingsPrefs)
  }

  @Test
  fun updateSelectedPlaybackQuality() {
    settingsRepository.updateSelectedPlaybackQuality(1024)
    verify(settingsPrefs).savePlaybackQuality(1024)
    verifyNoMoreInteractions(settingsPrefs)
  }

  @Test
  fun getPlaybackSpeed() {
    whenever(settingsPrefs.getPlaybackSpeed()).doReturn(1.0f)
    val result = settingsRepository.getPlaybackSpeed()
    result isEqualTo 1.0f
    verify(settingsPrefs).getPlaybackSpeed()
    verifyNoMoreInteractions(settingsPrefs)
  }

  @Test
  fun getPlaybackQuality() {
    whenever(settingsPrefs.getPlaybackQuality()).doReturn(1024)
    val result = settingsRepository.getPlaybackQuality()
    result isEqualTo 1024
    verify(settingsPrefs).getPlaybackQuality()
    verifyNoMoreInteractions(settingsPrefs)
  }

  @Test
  fun getSubtitleLanguage() {
    whenever(settingsPrefs.getSubtitleLanguage()).doReturn("en")
    val result = settingsRepository.getSubtitleLanguage()
    result isEqualTo "en"
    verify(settingsPrefs).getSubtitleLanguage()
    verifyNoMoreInteractions(settingsPrefs)
  }

  @Test
  fun getAutoPlayNextAllowed() {
    whenever(settingsPrefs.getAutoPlayAllowed()).doReturn(true)
    val result = settingsRepository.getAutoPlayNextAllowed()
    result isEqualTo true
    verify(settingsPrefs).getAutoPlayAllowed()
    verifyNoMoreInteractions(settingsPrefs)
  }

  @Test
  fun getDownloadQuality() {
    whenever(settingsPrefs.getDownloadQuality()).doReturn("hd")
    val result = settingsRepository.getDownloadQuality()
    result isEqualTo "hd"
    verify(settingsPrefs).getDownloadQuality()
    verifyNoMoreInteractions(settingsPrefs)
  }

  @Test
  fun getDownloadsWifiOnly() {
    whenever(settingsPrefs.getDownloadsWifiOnly()).doReturn(true)
    val result = settingsRepository.getDownloadsWifiOnly()
    result isEqualTo true
    verify(settingsPrefs).getDownloadsWifiOnly()
    verifyNoMoreInteractions(settingsPrefs)
  }

  @Test
  fun isOnboardingAllowed() {
    whenever(settingsPrefs.isOnboardingAllowed()).doReturn(true)
    val result = settingsRepository.isOnboardingAllowed()
    result isEqualTo false
//    verify(settingsPrefs).isOnboardingAllowed()
    //   verifyNoMoreInteractions(settingsPrefs)
  }

  @Test
  fun getOnboardedViews() {
    whenever(settingsPrefs.getOnboardedViews()).doReturn(listOf("Download"))
    val result = settingsRepository.getOnboardedViews()
    result isEqualTo listOf(OnboardingView.Download)
    verify(settingsPrefs).getOnboardedViews()
    verifyNoMoreInteractions(settingsPrefs)
  }

  @Test
  fun logout() {
    testCoroutineRule.runBlockingTest {
      settingsRepository.logout()
      verify(contentDataSourceLocal).deleteAll()
      verify(progressionDataSourceLocal).deleteWatchStats()
      verify(settingsPrefs).clear()
      verifyNoMoreInteractions(contentDataSourceLocal)
      verifyNoMoreInteractions(settingsPrefs)
    }
  }

  @Test
  fun updateSelectedDownloadQuality() {
    settingsRepository.updateSelectedDownloadQuality("hd")
    verify(settingsPrefs).saveDownloadQuality("hd")
    verifyNoMoreInteractions(settingsPrefs)
  }

  @Test
  fun updateDownloadsWifiOnly() {
    settingsRepository.updateDownloadsWifiOnly(true)
    verify(settingsPrefs).saveDownloadsWifiOnly(true)
    verifyNoMoreInteractions(settingsPrefs)
  }

  @Test
  fun updateOnboardingAllowed() {
    settingsRepository.updateOnboardingAllowed(false)
    verify(settingsPrefs).saveOnboardingAllowed(false)
    verifyNoMoreInteractions(settingsPrefs)
  }

  @Test
  fun updateOnboardedView() {
    settingsRepository.updateOnboardedView(OnboardingView.Download)
    verify(settingsPrefs).saveOnboardedView("Download")
    verifyNoMoreInteractions(settingsPrefs)
  }

  @Test
  fun getLoggedInUser() {
    settingsRepository.getLoggedInUser()
    verify(loginPrefs).getLoggedInUser()
    verifyNoMoreInteractions(loginPrefs)
  }
}
