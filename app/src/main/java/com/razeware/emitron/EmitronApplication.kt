package com.razeware.emitron

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class for Emitron
 */
@HiltAndroidApp
class EmitronApplication : Application(), Configuration.Provider {

  /**
   * Used to inject WorkManager workers using Hilt.
   * */
  @Inject
  lateinit var workerFactory: HiltWorkerFactory

  /**
   * Provides a simple configuration to build Workers with Hilt.
   * */
  override fun getWorkManagerConfiguration() =
    Configuration.Builder()
      .setWorkerFactory(workerFactory)
      .build()
}