package com.razeware.emitron

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.razeware.emitron.di.DaggerAppComponent
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
   * See [Application.onCreate]
   */
  override fun onCreate() {
    super.onCreate()
    DaggerAppComponent.builder().app(this)
      .build()
      .inject(this)
  }

  /**
   * @return injector for Android components
   */
  override fun androidInjector(): AndroidInjector<Any> = androidInjector

}
