package com.raywenderlich.emitron.ui.settings

import androidx.lifecycle.ViewModel
import com.raywenderlich.emitron.data.login.LoginRepository
import javax.inject.Inject

/**
 * ViewModel for settings view
 */
class SettingsViewModel @Inject constructor(private val loginRepository: LoginRepository) :
  ViewModel() {

  /**
   * Logout user
   */
  fun logout() {
    loginRepository.deleteUser()
  }
}
