package com.raywenderlich.emitron.di.modules

import com.raywenderlich.emitron.prefs.PrefUtils
import com.raywenderlich.emitron.network.RequestHelper
import dagger.Module
import dagger.Provides

@Module
abstract class SessionModule {

  @Module
  companion object {

    @JvmStatic
    @Provides
    fun provideRequestHelper(prefUtils: PrefUtils): RequestHelper =
      RequestHelper(apiAuthToken = prefUtils.getApiAuthToken())
  }
}
