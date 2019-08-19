package com.raywenderlich.emitron.data.login

import com.raywenderlich.emitron.model.Content
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
  fun isLoggedIn() = loginPrefs.isLoggedIn()

  /**
   * Check if user has subscription
   *
   * @return True if user has subscription, otherwise False
   */
  fun hasSubscription() = loginPrefs.hasSubscription()

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
   * @param hasSubscription subscription status
   */
  fun storeHasSubscription(hasSubscription: Boolean) {
    loginPrefs.storeHasSubscription(hasSubscription)
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
   * Get subscription for current user
   *
   * @return Subscription API response
   */
  suspend fun getSubscription(): Content {
    return withContext(threadManager.io) {
      loginApi.getSubscription()
    }
  }
}
