package com.raywenderlich.emitron.data.login

import com.raywenderlich.emitron.model.Contents
import com.raywenderlich.emitron.network.AuthInterceptor
import com.raywenderlich.emitron.utils.async.ThreadManager
import com.raywenderlich.guardpost.data.SSOUser
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Repository for session operations
 */
class LoginRepository @Inject constructor(
  private val loginApi: LoginApi,
  private val loginPrefs: LoginPrefs,
  private val authInterceptor: AuthInterceptor,
  private val threadManager: ThreadManager
) {

  /**
   * Check if user is logged in
   *
   * @return True if user is logged in, otherwise False
   */
  fun isLoggedIn(): Boolean = loginPrefs.isLoggedIn()

  /**
   * Check if user has permissions
   *
   * @return True if user has permissions, otherwise False
   */
  fun hasPermissions(): Boolean = loginPrefs.getPermissions().isNotEmpty()

  /**
   * Store the user to preferences
   *
   * @param ssoUser User details
   */
  fun storeUser(ssoUser: SSOUser) {
    loginPrefs.storeUser(ssoUser)
    authInterceptor.updateAuthToken(loginPrefs.authToken())
  }

  /**
   * Store if user has subscription
   *
   * @param subscriptions List of subscriptions
   */
  fun updatePermissions(subscriptions: List<String>) {
    loginPrefs.savePermissions(subscriptions)
  }

  /**
   * Delete saved user details
   *
   */
  fun deleteUser() {
    loginPrefs.clear()
    authInterceptor.clear()
  }

  /**
   * Get permissions for current user
   *
   * @return Permission API response
   */
  @Throws(Exception::class)
  suspend fun getPermissions(): Contents {
    return withContext(threadManager.io) {
      loginApi.getPermissions()
    }
  }
}
