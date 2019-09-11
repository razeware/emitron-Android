package com.raywenderlich.emitron.utils

import android.util.Log
import com.crashlytics.android.Crashlytics
import com.raywenderlich.emitron.BuildConfig

object Log {

  fun debug(message: String, tag: String? = BuildConfig.APPLICATION_ID.toUpperCase()) {
    if (BuildConfig.DEBUG || Log.isLoggable(tag, Log.DEBUG)) {
      Log.d(tag, message)
    }
  }

  fun exception(e: Throwable) {
    if (BuildConfig.DEBUG) {
      e.printStackTrace()
    } else {
      Crashlytics.logException(e)
    }
  }

  fun error(e: Error) {
    if (BuildConfig.DEBUG) {
      e.printStackTrace()
    } else {
      Crashlytics.logException(e)
    }
  }
}
