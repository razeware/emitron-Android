package com.raywenderlich.emitron.di.modules

import android.app.Application
import com.raywenderlich.emitron.data.EmitronDatabase
import com.raywenderlich.emitron.data.content.dao.*
import com.raywenderlich.emitron.data.filter.dao.CategoryDao
import com.raywenderlich.emitron.data.filter.dao.DomainDao
import com.raywenderlich.emitron.data.progressions.dao.ProgressionDao
import com.raywenderlich.emitron.data.progressions.dao.WatchStatDao
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
    fun provideProgressionDao(application: Application): ProgressionDao =
      EmitronDatabase.getInstance(application).progressionDao()

    @JvmStatic
    @Provides
    fun provideGroupDao(application: Application): GroupDao =
      EmitronDatabase.getInstance(application).groupDao()

    @JvmStatic
    @Provides
    fun provideContentGroupJoinDao(application: Application): ContentGroupJoinDao =
      EmitronDatabase.getInstance(application).contentGroupDao()

    @JvmStatic
    @Provides
    fun provideGroupEpisodeJoinDao(application: Application): GroupEpisodeJoinDao =
      EmitronDatabase.getInstance(application).groupEpisodeDao()

    @JvmStatic
    @Provides
    fun provideDownloadDao(application: Application): DownloadDao =
      EmitronDatabase.getInstance(application).downloadDao()

    @JvmStatic
    @Provides
    fun provideWatchStatDao(application: Application): WatchStatDao =
      EmitronDatabase.getInstance(application).watchStateDao()
  }
}
