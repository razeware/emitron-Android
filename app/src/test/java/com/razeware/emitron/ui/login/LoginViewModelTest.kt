package com.razeware.emitron.ui.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.raywenderlich.guardpost.data.SSOUser
import com.razeware.emitron.data.login.LoginRepository
import com.razeware.emitron.utils.TestCoroutineRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

  private val loginRepository: LoginRepository = mock()

  private val permissionActionDelegate: PermissionActionDelegate = mock()

  private lateinit var viewModel: LoginViewModel

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  @Before
  fun setUp() {
    viewModel = LoginViewModel(loginRepository, permissionActionDelegate)
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
      // When
      viewModel.getPermissions()

      // Then
      verify(permissionActionDelegate).fetchPermissions()
      verifyNoMoreInteractions(permissionActionDelegate)
    }
  }
}
