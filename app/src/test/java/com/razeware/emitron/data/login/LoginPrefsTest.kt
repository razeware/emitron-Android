package com.razeware.emitron.data.login

import com.nhaarman.mockitokotlin2.*
import com.raywenderlich.android.preferences.PrefUtils
import com.raywenderlich.guardpost.data.SSOUser
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyString

class LoginPrefsTest {

  private lateinit var loginPrefs: LoginPrefs

  private val prefUtils: PrefUtils = mock()

  @Before
  fun setUp() {
    whenever(prefUtils.get(anyString(), anyBoolean())).doReturn(true)
    whenever(prefUtils.set(anyString(), anyBoolean())).doReturn(prefUtils)
    whenever(prefUtils.set(anyString(), anyString())).doReturn(prefUtils)
    whenever(prefUtils.get(anyString(), anyString())).doReturn("")
    whenever(prefUtils.commit()).doReturn(t = true)
    loginPrefs = LoginPrefs(prefUtils)
  }

  @Test
  fun storeUser() {
    val user = SSOUser()
    loginPrefs.storeUser(user)
    verify(prefUtils).set("user_auth_token", user.token)
    verify(prefUtils).set("user_username", user.username)
    verify(prefUtils).set("user_avatar_url", user.avatarUrl)
    verify(prefUtils).set("is_logged_in", true)
  }

  @Test
  fun clear() {
    loginPrefs.clear()
    verify(prefUtils).clear()
  }

  @Test
  fun isLoggedIn() {
    loginPrefs.isLoggedIn()
    verify(prefUtils).get("is_logged_in", false)
  }

  @Test
  fun authToken() {
    loginPrefs.authToken()
    verify(prefUtils).get("user_auth_token", "")
  }

  @Test
  fun storePermissions() {
    loginPrefs.savePermissions(listOf("download-videos"))
    verify(prefUtils).init("accounts")
    verify(prefUtils).set("user_permissions", "download-videos")
    verify(prefUtils).commit()
    verifyNoMoreInteractions(prefUtils)
  }

  @Test
  fun getPermissions() {
    whenever(prefUtils.get("user_permissions", "")).doReturn("")
    loginPrefs.getPermissions()
    verify(prefUtils).init("accounts")
    verify(prefUtils).get("user_permissions", "")
    verifyNoMoreInteractions(prefUtils)
  }

  @Test
  fun removePermissions() {
    loginPrefs.removePermissions()
    verify(prefUtils).init("accounts")
    verify(prefUtils).set("user_permissions", "")
    verify(prefUtils).commit()
  }
}
