package com.raywenderlich.emitron.di.modules

import com.raywenderlich.emitron.di.impl.SessionManagerImpl
import com.raywenderlich.emitron.di.utils.RequestHelper
import com.raywenderlich.emitron.utils.PrefUtils
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class SessionModule {

  @Binds
  abstract fun provideSessionManager(sessionManager: SessionManagerImpl): com.raywenderlich.emitron.di.impl.SessionManager

  @Module
  companion object {

    @JvmStatic
    @Provides
    fun provideRequestHelper(prefUtils: PrefUtils): RequestHelper =
      RequestHelper(apiAuthToken = prefUtils.getApiAuthToken())
  }
}
