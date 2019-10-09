package com.raywenderlich.emitron

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.raywenderlich.emitron.di.DaggerAppComponent
import com.raywenderlich.emitron.di.modules.worker.WorkerFactory
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

/**
 * Application class for Emitron
 */
class EmitronApplication : Application(), HasAndroidInjector {

  /**
   * Injector for Android components
   */
  @Inject
  lateinit var androidInjector: DispatchingAndroidInjector<Any>

  /**
   * Delegate class to handle app lifecycle events
   */
  @Inject
  lateinit var appLifeCycleDelegate: AppLifeCycleDelegate

  /**
   * Factory for workers
   */
  @Inject
  lateinit var workerFactory: WorkerFactory

  /**
   * See [Application.onCreate]
   */
  override fun onCreate() {
    super.onCreate()
    DaggerAppComponent.builder().app(this)
      .build()
      .inject(this)

    WorkManager.initialize(
      this,
      Configuration.Builder().setWorkerFactory(workerFactory).build()
    )
  }

  /**
   * @return injector for Android components
   */
  override fun androidInjector(): AndroidInjector<Any> = androidInjector

}
