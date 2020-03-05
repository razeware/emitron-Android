package com.razeware.emitron.di.modules

import android.app.Application
import com.razeware.emitron.data.EmitronDatabase
import com.razeware.emitron.data.content.dao.*
import com.razeware.emitron.data.filter.dao.CategoryDao
import com.razeware.emitron.data.filter.dao.DomainDao
import com.razeware.emitron.data.progressions.dao.ProgressionDao
import com.razeware.emitron.data.progressions.dao.WatchStatDao
import dagger.Module
import dagger.Provides

/**
 * Data module
 */
@Module
class DataModule {

  @Module
  companion object {

    /**
     * [DomainDao]
     */
    @JvmStatic
    @Provides
    fun provideDomainDao(application: Application): DomainDao =
      EmitronDatabase.getInstance(application).domainDao()

    /**
     * [CategoryDao]
     */
    @JvmStatic
    @Provides
    fun provideCategoryDao(application: Application): CategoryDao =
      EmitronDatabase.getInstance(application).categoryDao()

    /**
     * [ContentDao]
     */
    @JvmStatic
    @Provides
    fun provideContentDao(application: Application): ContentDao =
      EmitronDatabase.getInstance(application).contentDao()

    /**
     * [ContentDomainJoinDao]
     */
    @JvmStatic
    @Provides
    fun provideContentDomainJoinDao(application: Application): ContentDomainJoinDao =
      EmitronDatabase.getInstance(application).contentDomainJoinDao()

    /**
     * [ProgressionDao]
     */
    @JvmStatic
    @Provides
    fun provideProgressionDao(application: Application): ProgressionDao =
      EmitronDatabase.getInstance(application).progressionDao()

    /**
     * [GroupDao]
     */
    @JvmStatic
    @Provides
    fun provideGroupDao(application: Application): GroupDao =
      EmitronDatabase.getInstance(application).groupDao()

    /**
     * [ContentGroupJoinDao]
     */
    @JvmStatic
    @Provides
    fun provideContentGroupJoinDao(application: Application): ContentGroupJoinDao =
      EmitronDatabase.getInstance(application).contentGroupDao()

    /**
     * [GroupEpisodeJoinDao]
     */
    @JvmStatic
    @Provides
    fun provideGroupEpisodeJoinDao(application: Application): GroupEpisodeJoinDao =
      EmitronDatabase.getInstance(application).groupEpisodeDao()

    /**
     * [DownloadDao]
     */
    @JvmStatic
    @Provides
    fun provideDownloadDao(application: Application): DownloadDao =
      EmitronDatabase.getInstance(application).downloadDao()

    /**
     * [WatchStatDao]
     */
    @JvmStatic
    @Provides
    fun provideWatchStatDao(application: Application): WatchStatDao =
      EmitronDatabase.getInstance(application).watchStateDao()
  }
}
