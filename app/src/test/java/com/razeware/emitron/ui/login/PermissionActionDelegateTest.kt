package com.razeware.emitron.ui.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.*
import com.razeware.emitron.data.login.LoginRepository
import com.razeware.emitron.model.Attributes
import com.razeware.emitron.model.Contents
import com.razeware.emitron.model.Data
import com.razeware.emitron.utils.TestCoroutineRule
import com.razeware.emitron.utils.isEqualTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PermissionActionDelegateTest {

  private val loginRepository: LoginRepository = mock()

  private lateinit var viewModel: PermissionActionDelegate

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  @Before
  fun setUp() {
    viewModel = PermissionActionDelegate(loginRepository)
  }

  @Test
  fun isDownloadPermission() {
    whenever(loginRepository.isDownloadAllowed()).doReturn(true)

    val result = viewModel.isDownloadAllowed()

    result isEqualTo true
    verify(loginRepository).isDownloadAllowed()
    verifyNoMoreInteractions(loginRepository)
  }

  @Test
  fun getPermissions_noPermissions() {
    testCoroutineRule.runBlockingTest {
      whenever(loginRepository.getPermissions()).doReturn(Contents())
      viewModel.fetchPermissions()
      verify(loginRepository).getPermissions()
      Truth.assertThat(viewModel.permissionActionResult.value)
        .isEqualTo(PermissionActionDelegate.PermissionActionResult.NoPermission)
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

      viewModel.fetchPermissions()
      verify(loginRepository).getPermissions()
      Truth.assertThat(viewModel.permissionActionResult.value)
        .isEqualTo(PermissionActionDelegate.PermissionActionResult.HasPermission)
      verify(loginRepository).getPermissions()
      verify(loginRepository).isDownloadAllowed()
      verify(loginRepository).updatePermissions(listOf("stream-beginner-videos"))
      verifyNoMoreInteractions(loginRepository)
    }
  }

  @Test
  fun getPermissions_apiError() {
    testCoroutineRule.runBlockingTest {
      whenever(loginRepository.getPermissions()).doThrow(RuntimeException())

      viewModel.fetchPermissions()
      verify(loginRepository).getPermissions()
      Truth.assertThat(viewModel.permissionActionResult.value)
        .isEqualTo(PermissionActionDelegate.PermissionActionResult.PermissionRequestFailed)
      verifyNoMoreInteractions(loginRepository)
    }
  }
}
