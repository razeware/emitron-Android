package com.raywenderlich.emitron.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raywenderlich.emitron.data.login.LoginRepository
import com.raywenderlich.guardpost.data.SSOUser
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

/**
 * ViewModel for login view
 */
class LoginViewModel @Inject constructor(private val loginRepository: LoginRepository) :
  ViewModel() {

  /**
   * Possible login API results
   */
  enum class LoginActionResult {
    /**
     * User is logged in
     */
    LoggedIn,
    /**
     * User has no subscription
     */
    NoSubscription,
    /**
     * API request failed
     */
    SubscriptionRequestFailed
  }

  private val _loginActionResult = MutableLiveData<LoginActionResult>()

  /**
   * LiveData for login API results
   */
  val loginActionResult: LiveData<LoginActionResult>
    get() = _loginActionResult

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
   * Get subscriptions for the current logged in user
   */
  fun getSubscription() {
    viewModelScope.launch {
      try {
        val response = loginRepository.getSubscription()
        val hasSubscription = response.hasSubscription
        if (hasSubscription) {
          loginRepository.storeHasSubscription(hasSubscription)
          _loginActionResult.value = LoginActionResult.LoggedIn
        } else {
          _loginActionResult.value = LoginActionResult.NoSubscription
        }
      } catch (exception: RuntimeException) {
        _loginActionResult.value = LoginActionResult.SubscriptionRequestFailed
      } catch (exception: IOException) {
        _loginActionResult.value = LoginActionResult.SubscriptionRequestFailed
      }
    }
  }
}
