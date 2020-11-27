package com.raywenderlich.android.inappreview.di

import com.raywenderlich.android.inappreview.manager.InAppReviewManager
import com.raywenderlich.android.inappreview.manager.InAppReviewManagerImpl
import com.raywenderlich.android.inappreview.preferences.InAppReviewPreferences
import com.raywenderlich.android.inappreview.preferences.InAppReviewPreferencesImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

/**
 * Provides dependencies required for In App Review flow.
 * */
@Module
@InstallIn(ApplicationComponent::class)
abstract class InAppReviewBinds {

  /**
   * Provides Preferences wrapper.
   * */
  @Binds
  @Singleton
  abstract fun bindInAppReviewPreferences(
    inAppReviewPreferencesImpl: InAppReviewPreferencesImpl
  ): InAppReviewPreferences

  /**
   * Provides In App Review flow wrapper.
   * */
  @Binds
  @Singleton
  abstract fun bindInAppReviewManager(
    inAppReviewManagerImpl: InAppReviewManagerImpl
  ): InAppReviewManager
}