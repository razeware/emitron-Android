package com.raywenderlich.emitron.data.settings

import com.nhaarman.mockitokotlin2.*
import com.raywenderlich.emitron.data.prefs.PrefUtils
import com.raywenderlich.emitron.utils.isEqualTo
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers

class SettingsPrefsTest {

  private lateinit var settingsPref: SettingsPrefs

  private val prefUtils: PrefUtils = mock()

  @Before
  fun setUp() {
    whenever(prefUtils.set(any(), ArgumentMatchers.anyString())).doReturn(prefUtils)
    whenever(prefUtils.set(any(), ArgumentMatchers.anyBoolean())).doReturn(prefUtils)
    whenever(prefUtils.set(any(), ArgumentMatchers.anyInt())).doReturn(prefUtils)
    whenever(prefUtils.commit()).doReturn(t = true)
    settingsPref = SettingsPrefs(prefUtils)
  }

  @Test
  fun saveSearchQuery() {
    whenever(prefUtils.get("recent_searches", "")).doReturn("Android, Kotlin")

    settingsPref.saveSearchQuery("Emitron")

    verify(prefUtils).get("recent_searches", "")
    verify(prefUtils).set("recent_searches", "Emitron, Android, Kotlin")
  }

  @Test
  fun saveSearchQuery_queryExists() {
    whenever(prefUtils.get("recent_searches", "")).doReturn(
      "Android, Kotlin, Emitron"
    )

    settingsPref.saveSearchQuery("Emitron")

    verify(prefUtils).get("recent_searches", "")
    verify(prefUtils).set("recent_searches", "Emitron, Android, Kotlin")
  }

  @Test
  fun saveSearchQuery_minRecentItems() {
    whenever(prefUtils.get("recent_searches", "")).doReturn(
      "Android, Kotlin, Emitron, Core Data, SwiftUI, Room"
    )

    settingsPref.saveSearchQuery("Emitron")

    verify(prefUtils).get("recent_searches", "")
    verify(prefUtils).set("recent_searches", "Emitron, Android, Kotlin, Core Data, SwiftUI")
  }

  @Test
  fun getSearchQueries() {
    whenever(prefUtils.get("recent_searches", "")).doReturn(
      "Android, Kotlin, Emitron"
    )

    val result = settingsPref.getSearchQueries()
    result isEqualTo listOf("Android", "Kotlin", "Emitron")


    verify(prefUtils).get("recent_searches", "")
  }

  @Test
  fun getSearchQueries_NoSavedQueries() {
    whenever(prefUtils.get("recent_searches", "")).doReturn("")

    val result = settingsPref.getSearchQueries()
    result isEqualTo emptyList<String>()

    verify(prefUtils).get("recent_searches", "")
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
    verify(prefUtils).clear()
  }
}
