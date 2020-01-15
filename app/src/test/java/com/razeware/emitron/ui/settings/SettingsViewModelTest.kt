package com.razeware.emitron.ui.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import com.razeware.emitron.data.login.LoginRepository
import com.razeware.emitron.data.settings.SettingsRepository
import com.razeware.emitron.utils.isEqualTo
import com.razeware.emitron.utils.observeForTestingResult
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

  private val loginRepository: LoginRepository = mock()

  private val settingsRepository: SettingsRepository = mock()

  private lateinit var viewModel: SettingsViewModel

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @Before
  fun setUp() {
    // Given
    whenever(settingsRepository.getNightMode()).doReturn(1)
    whenever(settingsRepository.isCrashReportingAllowed()).doReturn(true)
    whenever(settingsRepository.getPlaybackSpeed()).doReturn(1.0f)
    whenever(settingsRepository.getPlaybackQuality()).doReturn(1)
    whenever(settingsRepository.getSubtitleLanguage()).doReturn("en")
    whenever(settingsRepository.getDownloadQuality()).doReturn("hd")
    whenever(settingsRepository.getDownloadsWifiOnly()).doReturn(true)

    viewModel = SettingsViewModel(loginRepository, settingsRepository)
  }

  @Test
  fun init() {
    // When
    val nightMode = viewModel.nightMode.observeForTestingResult()
    val crashReportingAllowed = viewModel.crashReportingAllowed.observeForTestingResult()
    val playbackSpeed = viewModel.playbackSpeed.observeForTestingResult()
    val playbackQuality = viewModel.playbackQuality.observeForTestingResult()
    val subtitleLanguage = viewModel.subtitlesLanguage.observeForTestingResult()
    val downloadQuality = viewModel.downloadQuality.observeForTestingResult()
    val downloadWifiOnly = viewModel.downloadsWifiOnly.observeForTestingResult()

    // Then
    verify(settingsRepository).getNightMode()
    verify(settingsRepository).isCrashReportingAllowed()
    verify(settingsRepository).getPlaybackQuality()
    verify(settingsRepository).getSubtitleLanguage()
    verify(settingsRepository).getPlaybackSpeed()
    verify(settingsRepository).getDownloadQuality()
    verify(settingsRepository).getDownloadsWifiOnly()
    verifyNoMoreInteractions(settingsRepository)

    nightMode isEqualTo 1
    crashReportingAllowed isEqualTo true
    playbackSpeed isEqualTo 1.0f
    playbackQuality isEqualTo 1
    subtitleLanguage isEqualTo "en"
    downloadQuality isEqualTo "hd"
    downloadWifiOnly isEqualTo true
  }

  @Test
  fun logout() {
    viewModel.logout()
    verify(loginRepository).deleteUser()
    verifyNoMoreInteractions(loginRepository)
  }

  @Test
  fun updateCrashReportingAllowed() {
    viewModel.updateCrashReportingAllowed(true)
    verify(settingsRepository).isCrashReportingAllowed()
    verify(settingsRepository).getPlaybackQuality()
    verify(settingsRepository).getSubtitleLanguage()
    verify(settingsRepository).getPlaybackSpeed()
    verify(settingsRepository).getNightMode()
    verify(settingsRepository).getDownloadQuality()
    verify(settingsRepository).getDownloadsWifiOnly()
    verify(settingsRepository).updateCrashReportingAllowed(true)
    verifyNoMoreInteractions(settingsRepository)
  }

  @Test
  fun updateNightMode() {
    viewModel.updateNightMode(1)
    val nightMode = viewModel.nightMode.observeForTestingResult()

    verify(settingsRepository).isCrashReportingAllowed()
    verify(settingsRepository).getPlaybackQuality()
    verify(settingsRepository).getSubtitleLanguage()
    verify(settingsRepository).getPlaybackSpeed()
    verify(settingsRepository).getNightMode()
    verify(settingsRepository).getDownloadQuality()
    verify(settingsRepository).getDownloadsWifiOnly()
    verify(settingsRepository).updateNightMode(1)
    verifyNoMoreInteractions(settingsRepository)
    assertThat(nightMode).isEqualTo(1)
  }

  @Test
  fun updatePlaybackSpeed() {
    viewModel.updatePlaybackSpeed(1.0f)
    val playbackSpeed = viewModel.playbackSpeed.observeForTestingResult()

    verify(settingsRepository).isCrashReportingAllowed()
    verify(settingsRepository).getPlaybackQuality()
    verify(settingsRepository).getSubtitleLanguage()
    verify(settingsRepository).getPlaybackSpeed()
    verify(settingsRepository).getNightMode()
    verify(settingsRepository).getPlaybackSpeed()
    verify(settingsRepository).getDownloadQuality()
    verify(settingsRepository).getDownloadsWifiOnly()
    verify(settingsRepository).updateSelectedPlaybackSpeed(1.0f)
    verifyNoMoreInteractions(settingsRepository)
    assertThat(playbackSpeed).isEqualTo(1.0f)
  }

  @Test
  fun updatePlaybackQuality() {
    viewModel.updatePlaybackQuality(1)
    val playbackQuality = viewModel.playbackQuality.observeForTestingResult()

    verify(settingsRepository).isCrashReportingAllowed()
    verify(settingsRepository).getPlaybackQuality()
    verify(settingsRepository).getSubtitleLanguage()
    verify(settingsRepository).getPlaybackSpeed()
    verify(settingsRepository).getNightMode()
    verify(settingsRepository).getDownloadQuality()
    verify(settingsRepository).getDownloadsWifiOnly()
    verify(settingsRepository).updateSelectedPlaybackQuality(1)
    verifyNoMoreInteractions(settingsRepository)
    assertThat(playbackQuality).isEqualTo(1)
  }

  @Test
  fun updateSubtitlesLanguage() {
    viewModel.updateSubtitlesLanguage("en")
    val subtitleLanguage = viewModel.subtitlesLanguage.observeForTestingResult()

    verify(settingsRepository).isCrashReportingAllowed()
    verify(settingsRepository).getPlaybackQuality()
    verify(settingsRepository).getSubtitleLanguage()
    verify(settingsRepository).getPlaybackSpeed()
    verify(settingsRepository).getNightMode()
    verify(settingsRepository).getDownloadQuality()
    verify(settingsRepository).getDownloadsWifiOnly()
    verify(settingsRepository).updateSubtitleLanguage("en")
    verifyNoMoreInteractions(settingsRepository)
    assertThat(subtitleLanguage).isEqualTo("en")
  }

  @Test
  fun getNightMode() {
    viewModel.getNightMode()
    verify(settingsRepository).isCrashReportingAllowed()
    verify(settingsRepository).getPlaybackQuality()
    verify(settingsRepository).getSubtitleLanguage()
    verify(settingsRepository).getPlaybackSpeed()
    verify(settingsRepository, times(2)).getNightMode()
    verify(settingsRepository).getDownloadQuality()
    verify(settingsRepository).getDownloadsWifiOnly()
    verifyNoMoreInteractions(settingsRepository)
  }

  @Test
  fun getPlaybackQuality() {
    viewModel.getPlaybackQuality()
    verify(settingsRepository).getNightMode()
    verify(settingsRepository).isCrashReportingAllowed()
    verify(settingsRepository).getSubtitleLanguage()
    verify(settingsRepository).getPlaybackSpeed()
    verify(settingsRepository, times(2)).getPlaybackQuality()
    verify(settingsRepository).getDownloadQuality()
    verify(settingsRepository).getDownloadsWifiOnly()
    verifyNoMoreInteractions(settingsRepository)
  }

  @Test
  fun getPlaybackSpeed() {
    viewModel.getPlaybackSpeed()
    verify(settingsRepository).getNightMode()
    verify(settingsRepository).isCrashReportingAllowed()
    verify(settingsRepository).getPlaybackQuality()
    verify(settingsRepository).getSubtitleLanguage()
    verify(settingsRepository, times(2)).getPlaybackSpeed()
    verify(settingsRepository).getDownloadQuality()
    verify(settingsRepository).getDownloadsWifiOnly()
    verifyNoMoreInteractions(settingsRepository)
  }

  @Test
  fun getSubtitleLanguage() {
    viewModel.getSubtitleLanguage()
    verify(settingsRepository).getNightMode()
    verify(settingsRepository).isCrashReportingAllowed()
    verify(settingsRepository).getPlaybackQuality()
    verify(settingsRepository).getPlaybackSpeed()
    verify(settingsRepository, times(2)).getSubtitleLanguage()
    verify(settingsRepository).getDownloadQuality()
    verify(settingsRepository).getDownloadsWifiOnly()
    verifyNoMoreInteractions(settingsRepository)
  }

  @Test
  fun updateDownloadQuality() {

    val downloadQuality = viewModel.downloadQuality.observeForTestingResult()

    verify(settingsRepository).getNightMode()
    verify(settingsRepository).isCrashReportingAllowed()
    verify(settingsRepository).getPlaybackQuality()
    verify(settingsRepository).getPlaybackSpeed()
    verify(settingsRepository).getSubtitleLanguage()
    verify(settingsRepository).getDownloadQuality()
    verify(settingsRepository).getDownloadsWifiOnly()

    viewModel.updateDownloadQuality("hd")
    verify(settingsRepository).updateSelectedDownloadQuality("hd")
    assertThat(downloadQuality).isEqualTo("hd")
    verifyNoMoreInteractions(settingsRepository)
  }

  @Test
  fun updateDownloadsWifiOnly() {
    val downloadsWifiOnly = viewModel.downloadsWifiOnly.observeForTestingResult()

    verify(settingsRepository).getNightMode()
    verify(settingsRepository).isCrashReportingAllowed()
    verify(settingsRepository).getPlaybackQuality()
    verify(settingsRepository).getPlaybackSpeed()
    verify(settingsRepository).getSubtitleLanguage()
    verify(settingsRepository).getDownloadQuality()
    verify(settingsRepository).getDownloadsWifiOnly()

    viewModel.updateDownloadsWifiOnly(true)
    verify(settingsRepository).updateDownloadsWifiOnly(true)
    assertThat(downloadsWifiOnly).isEqualTo(true)
    verifyNoMoreInteractions(settingsRepository)
  }

  @Test
  fun getDownloadQuality() {
    viewModel.getDownloadQuality()
    verify(settingsRepository).getNightMode()
    verify(settingsRepository).isCrashReportingAllowed()
    verify(settingsRepository).getPlaybackQuality()
    verify(settingsRepository).getPlaybackSpeed()
    verify(settingsRepository).getSubtitleLanguage()
    verify(settingsRepository, times(2)).getDownloadQuality()
    verify(settingsRepository).getDownloadsWifiOnly()
    verifyNoMoreInteractions(settingsRepository)
  }

  @Test
  fun getDownloadsWifiOnly() {
    viewModel.getDownloadsWifiOnly()
    verify(settingsRepository).getNightMode()
    verify(settingsRepository).isCrashReportingAllowed()
    verify(settingsRepository).getPlaybackQuality()
    verify(settingsRepository).getPlaybackSpeed()
    verify(settingsRepository).getSubtitleLanguage()
    verify(settingsRepository).getDownloadQuality()
    verify(settingsRepository, times(2)).getDownloadsWifiOnly()
    verifyNoMoreInteractions(settingsRepository)
  }
}
