package com.raywenderlich.emitron.ui.settings

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.raywenderlich.emitron.data.login.LoginRepository
import org.junit.Before
import org.junit.Test

class SettingsViewModelTest {

  private val loginRepository: LoginRepository = mock()

  private lateinit var viewModel: SettingsViewModel

  @Before
  fun setUp() {
    viewModel = SettingsViewModel(loginRepository)
  }

  @Test
  fun logout() {
    viewModel.logout()
    verify(loginRepository).deleteUser()
  }
}
