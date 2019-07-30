package com.raywenderlich.emitron.data.login

import com.raywenderlich.emitron.data.prefs.PrefUtils
import com.raywenderlich.guardpost.data.SSOUser
import javax.inject.Inject

/**
 * Prefs helper for account
 */
class LoginPrefs @Inject constructor(private val prefs: PrefUtils) {

  companion object {
    private const val IS_LOGGED_IN = "is_logged_in"
    private const val USER_AUTH_TOKEN = "user_auth_token"
    private const val USER_USERNAME = "user_username"
    private const val USER_AVATAR_URL = "user_avatar_url"
    private const val USER_HAS_SUBSCRIPTION = "user_has_subscription"
  }

  init {
    prefs.init("accounts")
  }

  /**
   * Store the user to preferences
   *
   * @param ssoUser User details
   */
  fun storeUser(ssoUser: SSOUser) {
    with(prefs) {
      set(USER_AUTH_TOKEN, ssoUser.token)
      set(USER_USERNAME, ssoUser.username)
      set(USER_AVATAR_URL, ssoUser.avatarUrl)
      set(IS_LOGGED_IN, true)
      commit()
    }
  }

  /**
   * Store if user has subscription
   *
   * @param hasSubscription subscription status
   */
  fun storeHasSubscription(hasSubscription: Boolean) {
    prefs.set(USER_HAS_SUBSCRIPTION, hasSubscription).commit()
  }

  /**
   * Clear preferences
   */
  fun clear() {
    prefs.clear()
  }

  /**
   * Check if user is logged in
   *
   * @return True if user is logged in, otherwise False
   */
  fun isLoggedIn(): Boolean = prefs.get(IS_LOGGED_IN, false)

  /**
   * Check if user has subscription
   *
   * @return True if user has subscription, otherwise False
   */
  fun hasSubscription(): Boolean = prefs.get(USER_HAS_SUBSCRIPTION, false)

  /**
   * Get the auth token for logged in user
   *
   * @return authToken if the user is logged in or empty string
   */
  fun authToken(): String = prefs.get(USER_AUTH_TOKEN, "")
}