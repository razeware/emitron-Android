package com.raywenderlich.emitron.di.modules

import android.app.Application
import com.raywenderlich.emitron.data.EmitronDatabase
import com.raywenderlich.emitron.data.content.dao.ContentDao
import com.raywenderlich.emitron.data.content.dao.ContentDomainJoinDao
import com.raywenderlich.emitron.data.filter.dao.CategoryDao
import com.raywenderlich.emitron.data.filter.dao.DomainDao
import com.raywenderlich.emitron.data.progressions.dao.ProgressionDao
import dagger.Module
import dagger.Provides

@Module
class DataModule {

  @Module
  companion object {

    @JvmStatic
    @Provides
    fun provideDomainDao(application: Application): DomainDao =
      EmitronDatabase.getInstance(application).domainDao()

    @JvmStatic
    @Provides
    fun provideCategoryDao(application: Application): CategoryDao =
      EmitronDatabase.getInstance(application).categoryDao()

    @JvmStatic
    @Provides
    fun provideContentDao(application: Application): ContentDao =
      EmitronDatabase.getInstance(application).contentDao()

    @JvmStatic
    @Provides
    fun provideContentDomainJoinDao(application: Application): ContentDomainJoinDao =
      EmitronDatabase.getInstance(application).contentDomainJoinDao()

    @JvmStatic
    @Provides
    fun provideProressionDao(application: Application): ProgressionDao =
      EmitronDatabase.getInstance(application).progressionDao()
  }
}
