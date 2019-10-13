package com.raywenderlich.emitron.di.modules

import android.app.Application
import com.raywenderlich.emitron.R
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
    fun provideRequestHelper(
      application: Application,
      loginPrefs: LoginPrefs
    ): RequestHelper =
      RequestHelper(
        appToken = application.getString(R.string.app_token),
        apiAuthToken = loginPrefs.authToken()
      )
  }
}
