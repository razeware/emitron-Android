package com.raywenderlich.emitron

import android.app.Activity
import android.app.Application
import androidx.fragment.app.Fragment
import com.crashlytics.android.Crashlytics
import com.raywenderlich.emitron.di.DaggerAppComponent
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.support.HasSupportFragmentInjector
import io.fabric.sdk.android.Fabric
import javax.inject.Inject


class EmitronApplication : Application(), HasSupportFragmentInjector, HasActivityInjector {

  @Inject
  lateinit var activityInjector: DispatchingAndroidInjector<Activity>

  @Inject
  lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

  @Inject
  lateinit var appLifeCycleDelegate: AppLifeCycleDelegate

  override fun onCreate() {
    super.onCreate()
    DaggerAppComponent.builder().app(this)
      .build()
      .inject(this)
    Fabric.with(this, Crashlytics())
  }

  override fun activityInjector() = activityInjector

  override fun supportFragmentInjector() = fragmentInjector

}
