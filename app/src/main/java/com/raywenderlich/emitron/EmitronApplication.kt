package com.raywenderlich.emitron

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.raywenderlich.emitron.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.fabric.sdk.android.Fabric
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
    Fabric.with(this, Crashlytics())
  }

  /**
   * @return injector for Android components
   */
  override fun androidInjector(): AndroidInjector<Any> = androidInjector

}
