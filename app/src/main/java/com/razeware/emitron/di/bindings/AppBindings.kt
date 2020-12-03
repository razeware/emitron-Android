package com.razeware.emitron.di.bindings

import com.razeware.emitron.network.AuthInterceptor
import com.razeware.emitron.network.AuthInterceptorImpl
import com.razeware.emitron.utils.Logger
import com.razeware.emitron.utils.LoggerImpl
import com.razeware.emitron.utils.async.ThreadManager
import com.razeware.emitron.utils.async.ThreadManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent

@Module
@InstallIn(ApplicationComponent::class)
abstract class AppBindings {

  @Binds
  abstract fun provideThreadManager(threadManager: ThreadManagerImpl): ThreadManager

  @Binds
  abstract fun provideAuthInterceptor(authInterceptor: AuthInterceptorImpl): AuthInterceptor

  @Binds
  abstract fun provideLogger(logger: LoggerImpl): Logger
}

