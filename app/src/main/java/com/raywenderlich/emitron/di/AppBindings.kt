package com.raywenderlich.emitron.di

import com.raywenderlich.emitron.MainActivity
import com.raywenderlich.emitron.di.bindings.FragmentBindings
import com.raywenderlich.emitron.di.bindings.ViewModelBindings
import com.raywenderlich.emitron.network.AuthInterceptor
import com.raywenderlich.emitron.network.AuthInterceptorImpl
import com.raywenderlich.emitron.utils.async.ThreadManager
import com.raywenderlich.emitron.utils.async.ThreadManagerImpl
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

  @Binds
  abstract fun provideThreadManager(schedulerProvider: ThreadManagerImpl): ThreadManager

  @Binds
  abstract fun provideAuthInterceptor(authInterceptor: AuthInterceptorImpl): AuthInterceptor
}

