package com.razeware.emitron.di

import com.razeware.emitron.MainActivity
import com.razeware.emitron.di.bindings.FragmentBindings
import com.razeware.emitron.di.bindings.ViewModelBindings
import com.razeware.emitron.network.AuthInterceptor
import com.razeware.emitron.network.AuthInterceptorImpl
import com.razeware.emitron.ui.download.DownloadService
import com.razeware.emitron.utils.async.ThreadManager
import com.razeware.emitron.utils.async.ThreadManagerImpl
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AppBindings {

  @ContributesAndroidInjector(
    modules = [
      FragmentBindings::class,
      ViewModelBindings::class
    ]
  )
  abstract fun contributeMainActivity(): MainActivity

  @ContributesAndroidInjector
  abstract fun contributeDownloadService(): DownloadService

  @Binds
  abstract fun provideThreadManager(schedulerProvider: ThreadManagerImpl): ThreadManager

  @Binds
  abstract fun provideAuthInterceptor(authInterceptor: AuthInterceptorImpl): AuthInterceptor
}

