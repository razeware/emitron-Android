package com.razeware.emitron.di.modules

import android.app.Application
import com.razeware.emitron.R
import com.razeware.emitron.data.login.LoginPrefs
import com.razeware.emitron.network.RequestHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
class SessionModule {

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
