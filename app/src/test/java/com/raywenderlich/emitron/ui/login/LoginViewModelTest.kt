package com.raywenderlich.emitron.ui.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import com.raywenderlich.emitron.data.login.LoginRepository
import com.raywenderlich.emitron.model.Attributes
import com.raywenderlich.emitron.model.Contents
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.utils.TestCoroutineRule
import com.raywenderlich.emitron.utils.isEqualTo
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
  fun getPermissions_noPermissions() {
    testCoroutineRule.runBlockingTest {
      whenever(loginRepository.getPermissions()).doReturn(Contents())
      viewModel.getPermissions()
      verify(loginRepository).getPermissions()
      assertThat(viewModel.loginActionResult.value)
        .isEqualTo(LoginViewModel.LoginActionResult.NoSubscription)
      verifyNoMoreInteractions(loginRepository)
    }
  }

  @Test
  fun getPermissions_hasPermissions() {
    testCoroutineRule.runBlockingTest {
      whenever(loginRepository.getPermissions()).doReturn(
        Contents(
          datum = listOf(Data(attributes = Attributes(tag = "stream-beginner-videos")))
        )
      )

      viewModel.getPermissions()
      verify(loginRepository).getPermissions()
      assertThat(viewModel.loginActionResult.value)
        .isEqualTo(LoginViewModel.LoginActionResult.LoggedIn)
      verify(loginRepository).getPermissions()
      verify(loginRepository).updatePermissions(listOf("stream-beginner-videos"))
      verifyNoMoreInteractions(loginRepository)
    }
  }

  @Test
  fun getPermissions_apiError() {
    testCoroutineRule.runBlockingTest {
      whenever(loginRepository.getPermissions()).doThrow(RuntimeException())

      viewModel.getPermissions()
      verify(loginRepository).getPermissions()
      assertThat(viewModel.loginActionResult.value)
        .isEqualTo(LoginViewModel.LoginActionResult.SubscriptionRequestFailed)
      verifyNoMoreInteractions(loginRepository)
    }
  }

  @Test
  fun hasDownloadPermission() {
    whenever(loginRepository.hasDownloadPermission()).doReturn(true)

    val result = viewModel.hasDownloadPermission()

    result isEqualTo true
    verify(loginRepository).hasDownloadPermission()
    verifyNoMoreInteractions(loginRepository)
  }

}
