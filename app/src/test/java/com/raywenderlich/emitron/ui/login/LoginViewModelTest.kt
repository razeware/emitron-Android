package com.raywenderlich.emitron.ui.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import com.raywenderlich.emitron.data.login.LoginRepository
import com.raywenderlich.emitron.model.Content
import com.raywenderlich.emitron.ui.utils.TestCoroutineRule
import com.raywenderlich.guardpost.data.SSOUser
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

  private val loginRepository: LoginRepository = mock()

  private lateinit var viewModel: LoginViewModel

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  @Before
  fun setUp() {
    viewModel = LoginViewModel(loginRepository)
  }

  @Test
  fun storeUser() {
    val user = SSOUser()
    viewModel.storeUser(user)
    verify(loginRepository).storeUser(user)
  }

  @Test
  fun deleteUser() {
    viewModel.deleteUser()
    verify(loginRepository).deleteUser()
  }

  @Test
  fun getSubscription_noSubscription() {
    testCoroutineRule.runBlockingTest {
      whenever(loginRepository.getSubscription()).doReturn(Content())

      viewModel.getSubscription()
      verify(loginRepository).getSubscription()
      assertThat(viewModel.loginActionResult.value)
        .isEqualTo(LoginViewModel.LoginActionResult.NoSubscription)
      verifyNoMoreInteractions(loginRepository)
    }
  }

  @Test
  fun getSubscription_hasSubscription() {
    testCoroutineRule.runBlockingTest {
      whenever(loginRepository.getSubscription()).doReturn(Content().apply {
        hasSubscription = true
      })

      viewModel.getSubscription()
      verify(loginRepository).getSubscription()
      assertThat(viewModel.loginActionResult.value)
        .isEqualTo(LoginViewModel.LoginActionResult.LoggedIn)
      verify(loginRepository).storeHasSubscription(true)
      verifyNoMoreInteractions(loginRepository)
    }
  }

  @Test
  fun getSubscription_apiError() {
    testCoroutineRule.runBlockingTest {
      whenever(loginRepository.getSubscription()).doThrow(RuntimeException())

      viewModel.getSubscription()
      verify(loginRepository).getSubscription()
      assertThat(viewModel.loginActionResult.value)
        .isEqualTo(LoginViewModel.LoginActionResult.SubscriptionRequestFailed)
      verifyNoMoreInteractions(loginRepository)
    }
  }
}
