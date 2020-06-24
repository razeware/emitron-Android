package com.razeware.emitron.data.login

import com.raywenderlich.guardpost.data.SSOUser
import com.razeware.emitron.model.Contents
import com.razeware.emitron.model.PermissionTag
import com.razeware.emitron.network.AuthInterceptor
import com.razeware.emitron.utils.async.ThreadManager
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
  fun hasPermissions(): Boolean {
    return loginPrefs.getPermissions().isNotEmpty() && (!loginPrefs.getPermissions()[0].isBlank())
  }

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
   * Remove user permissions
   *
   */
  fun removePermissions() {
    loginPrefs.removePermissions()
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

  /**
   * Check if user has download permission
   *
   * @return true if user has [PermissionTag.Download] permission
   */
  fun isDownloadAllowed(): Boolean =
    loginPrefs.getPermissions().contains(PermissionTag.Download.param)

  /**
   * Check if user has permission to stream professional courses
   */
  fun isProfessionalVideoPlaybackAllowed(): Boolean =
    loginPrefs.getPermissions().contains(PermissionTag.StreamProfessional.param)

  /**
   * Get stored permissions
   */
  fun getPermissionsFromPrefs(): List<String> = loginPrefs.getPermissions()
}
