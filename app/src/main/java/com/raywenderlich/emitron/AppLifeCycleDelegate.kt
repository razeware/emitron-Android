package com.raywenderlich.emitron

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Delegate class to handle all application lifecycle events.
 *
 * Remember not to do a lot of heavy lifting on the main thread,
 * as it will affect to app launch timing
 */
@Singleton
class AppLifeCycleDelegate @Inject constructor(val application: Application) : LifecycleObserver {

  /**
   * True if the app is cold started, else False
   */
  private var isNewLaunch = false

  /**
   * True if the app is in foreground, else False
   */
  private var isInForeground = false

  /**
   * Application lifecycle delegate functions
   */
  @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
  fun onLaunch() {
    // App is cold started
    isNewLaunch = true
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
  fun onResume() {
    // App is resumed
    isInForeground = true
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
  fun onPause() {
    // App is being sent to background
    isInForeground = false
  }
}
