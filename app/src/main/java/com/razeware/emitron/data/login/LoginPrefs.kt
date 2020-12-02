package com.razeware.emitron.data.login

import com.raywenderlich.guardpost.data.SSOUser
import com.raywenderlich.android.preferences.PrefUtils
import javax.inject.Inject

/**
 * Prefs helper for user account
 *
 * Data related to user account preferences should be stored and accessed using [LoginPrefs]
 */
class LoginPrefs @Inject constructor(private val prefs: com.raywenderlich.android.preferences.PrefUtils) {

  companion object {
    private const val IS_LOGGED_IN = "is_logged_in"
    private const val USER_AUTH_TOKEN = "user_auth_token"
    private const val USER_USERNAME = "user_username"
    private const val USER_AVATAR_URL = "user_avatar_url"
    private const val USER_PERMISSIONS = "user_permissions"
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
   * Get the auth token for logged in user
   *
   * @return authToken if the user is logged in or empty string
   */
  fun authToken(): String = prefs.get(USER_AUTH_TOKEN, "")

  /**
   * Save user permissions
   *
   * @param permissions Allowed permissions
   */
  fun savePermissions(permissions: List<String>) {
    val permissionsStr =
      permissions.toString()
        .replace("[", "")
        .replace("]", "")
    prefs.set(USER_PERMISSIONS, permissionsStr).commit()
  }

  /**
   * Remove user permissions
   */
  fun removePermissions() {
    prefs.set(USER_PERMISSIONS, "").commit()
  }

  /**
   * Get saved permissions
   *
   * @return List of permissions
   */
  fun getPermissions(): List<String> =
    prefs.get(USER_PERMISSIONS, "").split(",").map { it.trim() }

	/**
	 * Get logged in user
	 *
	 * @return logged in user name
	 */
	fun getLoggedInUser(): String =
		prefs.get(USER_USERNAME, "")
}
