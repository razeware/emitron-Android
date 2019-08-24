package com.raywenderlich.emitron

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
class AppLifeCycleDelegate @Inject constructor() : LifecycleObserver {

  /**
   * True if the app is cold started, else False
   */
  private var isNewLaunch = false

  /**
   * True if the app is in foreground, else False
   */
  private var isInForeground = false

  /**
   * See [Lifecycle.Event.ON_CREATE]
   */
  @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
  fun onLaunch() {
    // App is cold started
    isNewLaunch = true
  }

  /**
   * See [Lifecycle.Event.ON_RESUME]
   */
  @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
  fun onResume() {
    // App is resumed
    isInForeground = true
  }

  /**
   * See [Lifecycle.Event.ON_PAUSE]
   */
  @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
  fun onPause() {
    // App is being sent to background
    isInForeground = false
  }
}
