package com.raywenderlich.emitron.data.login

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.raywenderlich.emitron.data.prefs.PrefUtils
import com.raywenderlich.guardpost.data.SSOUser
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyBoolean

class LoginPrefsTest {

  private lateinit var loginPrefs: LoginPrefs

  private val prefUtils: PrefUtils = mock()

  @Before
  fun setUp() {
    whenever(prefUtils.get(any(), anyBoolean())).thenReturn(true)
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
  fun storeHasSubscription() {
    loginPrefs.storeHasSubscription(true)
    verify(prefUtils).set("user_has_subscription", true)
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
  fun hasSubscription() {
    loginPrefs.hasSubscription()
    verify(prefUtils).get("user_has_subscription", false)
  }

  @Test
  fun authToken() {
    loginPrefs.authToken()
    verify(prefUtils).get("user_auth_token", "")
  }
}
