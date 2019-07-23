package com.raywenderlich.emitron

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLifeCycleDelegate @Inject constructor(val application: Application) :
  Application.ActivityLifecycleCallbacks, LifecycleObserver {

  private var isNewLaunch = false

  var isInForeground = false

  @JvmField
  var foregroundActivityName = ""

  /**
   * Application lifecycle delegate functions
   */
  @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
  fun onLaunch() {
    isNewLaunch = true
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  fun onStart() {
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
  fun onResume() {
    isInForeground = true
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
  fun onPause() {
    isInForeground = false
  }

  /**
   * Activity lifecycle delegate functions
   */
  override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
  }

  override fun onActivityStarted(activity: Activity) {
  }

  override fun onActivityResumed(activity: Activity) {
  }

  override fun onActivityPaused(activity: Activity) {
    foregroundActivityName = ""

  }

  override fun onActivityStopped(activity: Activity) {
  }

  override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
  }

  override fun onActivityDestroyed(activity: Activity) {
  }
}
