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
    whenever(prefUtils.get(any(), ArgumentMatchers.anyString())).doReturn("")
    whenever(prefUtils.set(any(), ArgumentMatchers.anyString())).doReturn(prefUtils)
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
  fun clear() {
    settingsPref.clear()
    verify(prefUtils).clear()
  }
}
