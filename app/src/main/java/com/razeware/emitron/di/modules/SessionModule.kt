package com.razeware.emitron.di.modules

import android.app.Application
import com.razeware.emitron.R
import com.razeware.emitron.data.login.LoginPrefs
import com.razeware.emitron.network.RequestHelper
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
