package com.razeware.emitron.data.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import com.raywenderlich.guardpost.data.SSOUser
import com.razeware.emitron.model.Contents
import com.razeware.emitron.network.AuthInterceptor
import com.razeware.emitron.utils.TestCoroutineRule
import com.razeware.emitron.utils.async.ThreadManager
import com.razeware.emitron.utils.isEqualTo
import kotlinx.coroutines.Dispatchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginRepositoryTest {

  private val loginApi: LoginApi = mock()

  private val loginPrefs: LoginPrefs = mock()

  private val authInterceptor: AuthInterceptor = mock()

  private val threadManager: ThreadManager = mock()

  private lateinit var repository: LoginRepository

  @get:Rule
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  val testCoroutineRule: TestCoroutineRule = TestCoroutineRule()

  @Before
  fun setUp() {
    repository = LoginRepository(loginApi, loginPrefs, authInterceptor, threadManager)
  }

  @Test
  fun isLoggedIn() {
    whenever(loginPrefs.isLoggedIn()).doReturn(true)
    val result = repository.isLoggedIn()
    verify(loginPrefs).isLoggedIn()
    verifyNoMoreInteractions(loginPrefs)
    assertThat(result).isTrue()
  }

  @Test
  fun storeUser() {
    whenever(loginPrefs.authToken()).doReturn("razeware")
    val user = SSOUser(token = "razeware")
    repository.storeUser(user)
    verify(loginPrefs).storeUser(user)
    verify(loginPrefs).authToken()
    verify(authInterceptor).updateAuthToken("razeware")
    verifyNoMoreInteractions(loginPrefs)
    verifyNoMoreInteractions(authInterceptor)
  }

  @Test
  fun deleteUser() {
    repository.deleteUser()
    verify(loginPrefs).clear()
    verify(authInterceptor).clear()
    verifyNoMoreInteractions(loginPrefs)
    verifyNoMoreInteractions(authInterceptor)
  }

  @Test
  fun getPermissions() {
    testCoroutineRule.runBlockingTest {
      val expectedContent = Contents()
      whenever(loginApi.getPermissions()).doReturn(expectedContent)
      whenever(threadManager.io).doReturn(Dispatchers.Unconfined)
      val result = repository.getPermissions()
      verify(loginApi).getPermissions()
      assertThat(result).isEqualTo(expectedContent)
      verifyNoMoreInteractions(loginApi)
    }
  }


  @Test
  fun hasPermissions() {
    // Given
    whenever(loginPrefs.getPermissions()).doReturn(listOf("download-videos"))

    // Then
    repository.hasPermissions() isEqualTo true
  }

  @Test
  fun updatePermissions() {
    repository.updatePermissions(listOf("perm-1", "perm-2"))
    verify(loginPrefs).savePermissions(listOf("perm-1", "perm-2"))
    verifyNoMoreInteractions(loginPrefs)
  }

  @Test
  fun hasDownloadPermission() {
    // Given
    whenever(loginPrefs.getPermissions()).doReturn(listOf("download-videos"))

    // Then
    repository.isDownloadAllowed() isEqualTo true
  }

  @Test
  fun hasStreamProPermission() {
    // Given
    whenever(loginPrefs.getPermissions()).doReturn(listOf("stream-professional-videos"))

    // Then
    repository.isProfessionalVideoPlaybackAllowed() isEqualTo true
  }

  @Test
  fun hasStreamProPermission_NoPermission() {
    // Given
    whenever(loginPrefs.getPermissions()).doReturn(listOf("stream-beginner-videos"))

    // Then
    repository.isProfessionalVideoPlaybackAllowed() isEqualTo false
  }

  @Test
  fun getPermissionsFromPrefs() {
    // Given
    whenever(loginPrefs.getPermissions()).doReturn(listOf("download-videos"))

    // Then
    repository.getPermissionsFromPrefs() isEqualTo listOf("download-videos")
  }

  @Test
  fun removePermissions() {
    // When
    repository.removePermissions()

    // Then
    verify(loginPrefs).removePermissions()
    verifyNoMoreInteractions(loginApi)
  }
}
