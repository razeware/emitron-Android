package com.raywenderlich.emitron.di.modules

import android.app.Application
import com.raywenderlich.emitron.data.EmitronDatabase
import dagger.Module
import dagger.Provides

@Module
class DataModule {

  @Module
  companion object {

    @JvmStatic
    @Provides
    fun provideDomainDao(application: Application) =
      EmitronDatabase.getInstance(application).domainDao()

    @JvmStatic
    @Provides
    fun provideCategoryDao(application: Application) =
      EmitronDatabase.getInstance(application).categoryDao()
  }


}
