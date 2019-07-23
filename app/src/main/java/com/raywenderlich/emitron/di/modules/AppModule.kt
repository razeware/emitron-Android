package com.raywenderlich.emitron.di.modules

import android.app.Application
import android.content.Context
import com.raywenderlich.emitron.di.modules.viewmodel.ViewModelModule
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Module(
  includes = [
    AndroidInjectionModule::class,
    ViewModelModule::class,
    NetModule::class,
    SessionModule::class
  ]
)
class AppModule {

  @Singleton
  @Provides
  fun provideApplicationContext(application: Application): Context = application

}
