package com.razeware.emitron.data.settings

import com.nhaarman.mockitokotlin2.*
import com.raywenderlich.android.preferences.GeneralSettingsPrefs
import com.raywenderlich.android.preferences.PrefUtils
import com.razeware.emitron.utils.isEqualTo
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers

class SettingsPrefsTest {

  private lateinit var settingsPref: GeneralSettingsPrefs

  private val prefUtils: PrefUtils = mock()

  @Before
  fun setUp() {
    whenever(prefUtils.set(any(), ArgumentMatchers.anyString())).doReturn(prefUtils)
    whenever(prefUtils.set(any(), ArgumentMatchers.anyBoolean())).doReturn(prefUtils)
    whenever(prefUtils.set(any(), ArgumentMatchers.anyInt())).doReturn(prefUtils)
    whenever(prefUtils.set(any(), ArgumentMatchers.anyFloat())).doReturn(prefUtils)
    whenever(prefUtils.commit()).doReturn(t = true)
    settingsPref = GeneralSettingsPrefs(prefUtils)
  }

  @Test
  fun saveSearchQuery() {
    whenever(prefUtils.get("recent_searches", "")).doReturn("Android, Kotlin")

    settingsPref.saveSearchQuery("Emitron")

    verify(prefUtils).init("settings")
    verify(prefUtils).get("recent_searches", "")
    verify(prefUtils).set("recent_searches", "Emitron, Android, Kotlin")
    verify(prefUtils).commit()
    verifyNoMoreInteractions(prefUtils)
  }

  @Test
  fun saveSearchQuery_queryExists() {
    whenever(prefUtils.get("recent_searches", "")).doReturn(
      "Android, Kotlin, Emitron"
    )

    settingsPref.saveSearchQuery("Emitron")

    verify(prefUtils).init("settings")
    verify(prefUtils).get("recent_searches", "")
    verify(prefUtils).set("recent_searches", "Emitron, Android, Kotlin")
    verify(prefUtils).commit()
    verifyNoMoreInteractions(prefUtils)
  }

  @Test
  fun saveSearchQuery_minRecentItems() {
    whenever(prefUtils.get("recent_searches", "")).doReturn(
      "Android, Kotlin, Emitron, Core Data, SwiftUI, Room"
    )

    settingsPref.saveSearchQuery("Emitron")

    verify(prefUtils).init("settings")
    verify(prefUtils).get("recent_searches", "")
    verify(prefUtils).set("recent_searches", "Emitron, Android, Kotlin, Core Data, SwiftUI")
    verify(prefUtils).commit()
    verifyNoMoreInteractions(prefUtils)
  }

  @Test
  fun getSearchQueries() {
    whenever(prefUtils.get("recent_searches", "")).doReturn(
      "Android, Kotlin, Emitron"
    )

    val result = settingsPref.getSearchQueries()
    result isEqualTo listOf("Android", "Kotlin", "Emitron")

    verify(prefUtils).init("settings")
    verify(prefUtils).get("recent_searches", "")
    verifyNoMoreInteractions(prefUtils)
  }

  @Test
  fun getSearchQueries_NoSavedQueries() {
    whenever(prefUtils.get("recent_searches", "")).doReturn("")

    val result = settingsPref.getSearchQueries()
    result isEqualTo emptyList<String>()
    verify(prefUtils).init("settings")
    verify(prefUtils).get("recent_searches", "")
    verifyNoMoreInteractions(prefUtils)
  }

  @Test
  fun saveCrashReportingAllowed() {
    settingsPref.saveCrashReportingAllowed(true)

    verify(prefUtils).init("settings")
    verify(prefUtils).set("allow_crash_reports", true)
    verify(prefUtils).commit()
    verifyNoMoreInteractions(prefUtils)
  }

  @Test
  fun saveNightMode() {
    settingsPref.saveNightMode(1)

    verify(prefUtils).init("settings")
    verify(prefUtils).set("selected_night_mode", 1)
    verify(prefUtils).commit()
    verifyNoMoreInteractions(prefUtils)
  }

  @Test
  fun isCrashReportingAllowed() {
    whenever(prefUtils.get("allow_crash_reports", false)).doReturn(true)

    val result = settingsPref.isCrashReportingAllowed()
    result isEqualTo true
    verify(prefUtils).init("settings")
    verify(prefUtils).get("allow_crash_reports", false)
    verifyNoMoreInteractions(prefUtils)
  }


  @Test
  fun getNightMode() {
    whenever(prefUtils.get("selected_night_mode", 2)).doReturn(1)

    val result = settingsPref.getNightMode()
    result isEqualTo 1
    verify(prefUtils).init("settings")
    verify(prefUtils).get("selected_night_mode", 2)
    verifyNoMoreInteractions(prefUtils)
  }

  @Test
  fun clear() {
    settingsPref.clear()
    verify(prefUtils).init("settings")
    verify(prefUtils).clear()
    verifyNoMoreInteractions(prefUtils)
  }

  @Test
  fun getPlaybackSpeed() {
    whenever(prefUtils.get("player_playback_speed", 1.0f)).doReturn(1.0f)

    val result = settingsPref.getPlaybackSpeed()
    result isEqualTo 1.0f
    verify(prefUtils).init("settings")
    verify(prefUtils).get("player_playback_speed", 1.0f)
    verifyNoMoreInteractions(prefUtils)
  }


  @Test
  fun getPlaybackQuality() {
    whenever(prefUtils.get("player_playback_quality", 1080)).doReturn(1)

    val result = settingsPref.getPlaybackQuality()
    result isEqualTo 1
    verify(prefUtils).init("settings")
    verify(prefUtils).get("player_playback_quality", 1080)
    verifyNoMoreInteractions(prefUtils)
  }

  @Test
  fun getAutoPlayAllowed() {
    whenever(prefUtils.get("player_auto_play_next", true)).doReturn(true)

    val result = settingsPref.getAutoPlayAllowed()
    result isEqualTo true
    verify(prefUtils).init("settings")
    verify(prefUtils).get("player_auto_play_next", true)
    verifyNoMoreInteractions(prefUtils)
  }


  @Test
  fun getSubtitleLanguage() {
    whenever(prefUtils.get("player_subtitles_language", "")).doReturn("en")

    val result = settingsPref.getSubtitleLanguage()
    result isEqualTo "en"
    verify(prefUtils).init("settings")
    verify(prefUtils).get("player_subtitles_language", "")
    verifyNoMoreInteractions(prefUtils)
  }

  @Test
  fun saveAutoPlayAllowed() {
    settingsPref.saveAutoPlayAllowed(true)

    verify(prefUtils).init("settings")
    verify(prefUtils).set("player_auto_play_next", true)
    verify(prefUtils).commit()
    verifyNoMoreInteractions(prefUtils)
  }

  @Test
  fun savePlaybackSpeed() {
    settingsPref.savePlaybackSpeed(1.0f)

    verify(prefUtils).init("settings")
    verify(prefUtils).set("player_playback_speed", 1.0f)
    verify(prefUtils).commit()
    verifyNoMoreInteractions(prefUtils)
  }

  @Test
  fun savePlaybackQuality() {
    settingsPref.savePlaybackQuality(1)

    verify(prefUtils).init("settings")
    verify(prefUtils).set("player_playback_quality", 1)
    verify(prefUtils).commit()
    verifyNoMoreInteractions(prefUtils)
  }

  @Test
  fun saveSubtitleLanguage() {
    settingsPref.saveSubtitleLanguage("en")

    verify(prefUtils).init("settings")
    verify(prefUtils).set("player_subtitles_language", "en")
    verify(prefUtils).commit()
    verifyNoMoreInteractions(prefUtils)
  }
}
