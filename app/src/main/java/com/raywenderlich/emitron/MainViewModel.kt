package com.raywenderlich.emitron

import androidx.lifecycle.ViewModel
import com.raywenderlich.emitron.data.login.LoginRepository
import javax.inject.Inject

/**
 * Parent Viewmodel for all fragment
 *
 * @param loginRepository [LoginRepository] to verify/get user login details
 */
class MainViewModel @Inject constructor(private val loginRepository: LoginRepository) :
  ViewModel() {

  /**
   * @return True if user is logged in, otherwise False
   */
  private fun isLoggedIn(): Boolean = loginRepository.isLoggedIn()

  /**
   * @return True if user has subscription, otherwise False
   */
  private fun hasSubscription(): Boolean = loginRepository.hasSubscription()

  /**
   * @return True if user is allowed to use app, otherwise False
   */
  fun isAllowed(): Boolean = isLoggedIn() && hasSubscription()

}
