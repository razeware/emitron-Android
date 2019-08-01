package com.raywenderlich.emitron

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.raywenderlich.emitron.data.login.LoginRepository
import org.junit.Before
import org.junit.Test

class MainViewModelTest {

  private val loginRepository: LoginRepository = mock()

  private lateinit var viewModel: MainViewModel

  @Before
  fun setUp() {
    viewModel = MainViewModel(loginRepository)
  }

  @Test
  fun isAllowed_A() {
    // Is logged in and has subscription
    whenever(loginRepository.isLoggedIn()).thenReturn(true)
    whenever(loginRepository.hasSubscription()).thenReturn(true)
    val result = viewModel.isAllowed()
    assertThat(result).isTrue()
    verify(loginRepository).isLoggedIn()
    verify(loginRepository).hasSubscription()
  }

  @Test
  fun isAllowed_B() {
    // Is logged in and has no subscription
    whenever(loginRepository.isLoggedIn()).thenReturn(true)
    whenever(loginRepository.hasSubscription()).thenReturn(false)
    val result = viewModel.isAllowed()
    assertThat(result).isFalse()
    verify(loginRepository).isLoggedIn()
    verify(loginRepository).hasSubscription()
  }
}
