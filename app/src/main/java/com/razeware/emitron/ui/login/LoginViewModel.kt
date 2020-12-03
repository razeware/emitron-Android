package com.razeware.emitron.ui.login

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raywenderlich.guardpost.data.SSOUser
import com.razeware.emitron.data.login.LoginRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for login view
 */
class LoginViewModel @ViewModelInject constructor(
  private val loginRepository: LoginRepository,
  private val permissionActionDelegate: PermissionActionDelegate
) : ViewModel(), PermissionsAction by permissionActionDelegate {

  /**
   * Store a user
   */
  fun storeUser(ssoUser: SSOUser) {
    loginRepository.storeUser(ssoUser)
  }


  /**
   * Delete a user
   */
  fun deleteUser() {
    loginRepository.deleteUser()
  }

  /**
   * Get permissions for the current logged in user
   */
  fun getPermissions() {
    viewModelScope.launch {
      permissionActionDelegate.fetchPermissions()
    }
  }
}
