package com.raywenderlich.emitron.data.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.raywenderlich.emitron.model.Content
import com.raywenderlich.emitron.network.AuthInterceptor
import com.raywenderlich.emitron.ui.utils.TestCoroutineRule
import com.raywenderlich.emitron.utils.async.ThreadManager
import com.raywenderlich.guardpost.data.SSOUser
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
    whenever(loginPrefs.isLoggedIn()).thenReturn(true)
    val result = repository.isLoggedIn()
    verify(loginPrefs).isLoggedIn()
    verifyNoMoreInteractions(loginPrefs)
    assertThat(result).isTrue()
  }

  @Test
  fun hasSubscription() {
    whenever(loginPrefs.hasSubscription()).thenReturn(true)
    val result = repository.hasSubscription()
    verify(loginPrefs).hasSubscription()
    verifyNoMoreInteractions(loginPrefs)
    assertThat(result).isTrue()
  }

  @Test
  fun storeUser() {
    whenever(loginPrefs.authToken()).thenReturn("Raywenderlich")
    val user = SSOUser(token = "Raywenderlich")
    repository.storeUser(user)
    verify(loginPrefs).storeUser(user)
    verify(loginPrefs).authToken()
    verify(authInterceptor).updateAuthToken("Raywenderlich")
    verifyNoMoreInteractions(loginPrefs)
    verifyNoMoreInteractions(authInterceptor)
  }

  @Test
  fun storeHasSubscription() {
    repository.storeHasSubscription(false)
    verify(loginPrefs).storeHasSubscription(false)
    repository.storeHasSubscription(true)
    verify(loginPrefs).storeHasSubscription(true)
    verifyNoMoreInteractions(loginPrefs)
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
  fun getSubscription() {
    testCoroutineRule.runBlockingTest {
      val expectedContent = Content()
      whenever(loginApi.getSubscription()).thenReturn(expectedContent)
      whenever(threadManager.io).thenReturn(Dispatchers.Unconfined)
      val result = repository.getSubscription()
      verify(loginApi).getSubscription()
      assertThat(result).isEqualTo(expectedContent)
      verifyNoMoreInteractions(loginApi)
    }
  }
}
