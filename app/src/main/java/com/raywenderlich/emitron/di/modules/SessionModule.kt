package com.raywenderlich.emitron.di.modules

import com.raywenderlich.emitron.data.login.LoginPrefs
import com.raywenderlich.emitron.network.RequestHelper
import dagger.Module
import dagger.Provides

@Module
abstract class SessionModule {

  @Module
  companion object {

    @JvmStatic
    @Provides
    fun provideRequestHelper(loginPrefs: LoginPrefs): RequestHelper =
      RequestHelper(apiAuthToken = loginPrefs.authToken())
  }
}
