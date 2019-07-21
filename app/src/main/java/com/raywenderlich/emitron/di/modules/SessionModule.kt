package com.raywenderlich.emitron.di.modules

import com.raywenderlich.emitron.utils.PrefUtils
import com.raywenderlich.emitron.utils.RequestHelper
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
