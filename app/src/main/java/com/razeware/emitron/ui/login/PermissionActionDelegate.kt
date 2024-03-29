package com.razeware.emitron.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.razeware.emitron.BuildConfig
import com.razeware.emitron.data.login.LoginRepository
import com.razeware.emitron.model.PermissionTag
import java.io.IOException
import javax.inject.Inject

/**
 * Session permission action
 */
interface PermissionsAction {

  /**
   * Get permissions
   */
  suspend fun fetchPermissions()

  /**
   * @return true if downloads can be shown, else false
   */
  fun isDownloadAllowed(): Boolean

  /**
   * @return true if professional videos can be played, else false
   */
  fun isProfessionalVideoPlaybackAllowed(): Boolean

  /**
   * @return true if beginner videos can be played, else false
   */
  fun isBeginnerVideoPlaybackAllowed(): Boolean

  /**
   * @return true if personal videos can be played, else false
   */
  fun isPersonalVideosPlaybackAllowed():Boolean

  /**
   * @return true if teams videos can be played, else false
   */
  fun isTeamsVideoPlaybackAllowed():Boolean

  /**
   * LiveData for permission action
   */
  val permissionActionResult: LiveData<PermissionActionDelegate.PermissionActionResult>
}

/**
 * [PermissionsAction] implementation
 */
class PermissionActionDelegate @Inject constructor(
  private val loginRepository: LoginRepository
) : PermissionsAction {

  /**
   * Possible login API results
   */
  enum class PermissionActionResult {
    /**
     * User is logged in
     */
    HasPermission,

    /**
     * User has download permission
     */
    HasDownloadPermission,

    /**
     * User has no subscription
     */
    NoPermission,

    /**
     * API request failed
     */
    PermissionRequestFailed
  }

  private val _permissionActionResult = MutableLiveData<PermissionActionResult>()

  override val permissionActionResult: LiveData<PermissionActionResult>
    get() = _permissionActionResult

  /**
   * We check if the user is in [Build`Config.DEBUG] mode to allow contributors
   * to use the app (we give them fake permissions).
   *
   * Get permissions for the current logged in user
   */
  override suspend fun fetchPermissions() {
    try {
      val response = loginRepository.getPermissions()
      val permissions = response.datum.mapNotNull {
        it.getTag()
      }

      if (permissions.isNotEmpty() || BuildConfig.DEBUG) {
        val userPermissions = PermissionTag.values().map { it.param }.toSet()
          .intersect(permissions).toList()
        loginRepository.updatePermissions(userPermissions)
        _permissionActionResult.value =
          PermissionActionResult.HasPermission

        if (isDownloadAllowed()) {
          _permissionActionResult.value =
            PermissionActionResult.HasDownloadPermission
        }
      } else {
        _permissionActionResult.value =
          PermissionActionResult.NoPermission
      }
    } catch (exception: RuntimeException) {
      _permissionActionResult.value =
        PermissionActionResult.PermissionRequestFailed
    } catch (exception: IOException) {
      _permissionActionResult.value =
        PermissionActionResult.PermissionRequestFailed
    }
  }

  override fun isDownloadAllowed(): Boolean = loginRepository.isDownloadAllowed()

  override fun isProfessionalVideoPlaybackAllowed(): Boolean =
    loginRepository.isProfessionalVideoPlaybackAllowed()

  override fun isBeginnerVideoPlaybackAllowed(): Boolean =
    loginRepository.isBeginnerVideoPlaybackAllowed()

  override fun isPersonalVideosPlaybackAllowed(): Boolean =
    loginRepository.isPersonalVideosPlayback()

  override fun isTeamsVideoPlaybackAllowed(): Boolean =
    loginRepository.isTeamsVideosPlayback()
}
