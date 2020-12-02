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
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent

/**
 * Data module
 */
@Module
@InstallIn(ApplicationComponent::class)
class DataModule {

  /**
   * [DomainDao]
   */
  @Provides
  fun provideDomainDao(application: Application): DomainDao =
    EmitronDatabase.getInstance(application).domainDao()

  /**
   * [CategoryDao]
   */
  @Provides
  fun provideCategoryDao(application: Application): CategoryDao =
    EmitronDatabase.getInstance(application).categoryDao()

  /**
   * [ContentDao]
   */
  @Provides
  fun provideContentDao(application: Application): ContentDao =
    EmitronDatabase.getInstance(application).contentDao()

  /**
   * [ContentDomainJoinDao]
   */
  @Provides
  fun provideContentDomainJoinDao(application: Application): ContentDomainJoinDao =
    EmitronDatabase.getInstance(application).contentDomainJoinDao()

  /**
   * [ProgressionDao]
   */
  @Provides
  fun provideProgressionDao(application: Application): ProgressionDao =
    EmitronDatabase.getInstance(application).progressionDao()

  /**
   * [GroupDao]
   */
  @Provides
  fun provideGroupDao(application: Application): GroupDao =
    EmitronDatabase.getInstance(application).groupDao()

  /**
   * [ContentGroupJoinDao]
   */
  @Provides
  fun provideContentGroupJoinDao(application: Application): ContentGroupJoinDao =
    EmitronDatabase.getInstance(application).contentGroupDao()

  /**
   * [GroupEpisodeJoinDao]
   */
  @Provides
  fun provideGroupEpisodeJoinDao(application: Application): GroupEpisodeJoinDao =
    EmitronDatabase.getInstance(application).groupEpisodeDao()

  /**
   * [DownloadDao]
   */
  @Provides
  fun provideDownloadDao(application: Application): DownloadDao =
    EmitronDatabase.getInstance(application).downloadDao()

  /**
   * [WatchStatDao]
   */
  @Provides
  fun provideWatchStatDao(application: Application): WatchStatDao =
    EmitronDatabase.getInstance(application).watchStateDao()
}
