package com.raywenderlich.android.preferences.di

import android.content.Context
import com.raywenderlich.android.preferences.PrefUtils
import com.raywenderlich.android.preferences.GeneralSettingsPrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class PreferencesModule {

  @Provides
  @Singleton
  fun providePrefsUtils(@ApplicationContext context: Context) = PrefUtils(context)

  @Provides
  @Singleton
  fun provideSettingsPrefs(prefUtils: PrefUtils) = GeneralSettingsPrefs(prefUtils)
}