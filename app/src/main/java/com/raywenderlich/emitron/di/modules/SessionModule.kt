package com.raywenderlich.emitron.di.modules

import com.raywenderlich.emitron.di.impl.SessionManagerImpl
import dagger.Binds
import dagger.Module

@Module
abstract class SessionModule {

  @Binds
  abstract fun provideSessionManager(sessionManager: SessionManagerImpl): com.raywenderlich.emitron.di.impl.SessionManager
}
