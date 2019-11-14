package com.raywenderlich.emitron.utils

import android.util.Log
import com.crashlytics.android.Crashlytics
import com.raywenderlich.emitron.BuildConfig
import javax.inject.Inject

/**
 * Logger
 */
interface Logger {
  /**
   * Log exception
   */
  fun log(e: Throwable)

  /**
   * Log error
   */
  fun log(e: Error)
}

/**
 * LoggerImpl
 */
class LoggerImpl @Inject constructor() : Logger {
  override fun log(e: Throwable) {
    com.raywenderlich.emitron.utils.Log.exception(e)
  }

  override fun log(e: Error) {
    com.raywenderlich.emitron.utils.Log.error(e)
  }
}

/**
 * Log Utils
 */
object Log {

  /**
   * Log debug message
   */
  fun debug(message: String, tag: String? = BuildConfig.APPLICATION_ID.toUpperCase()) {
    if (BuildConfig.DEBUG || Log.isLoggable(tag, Log.DEBUG)) {
      Log.d(tag, message)
    }
  }

  /**
   * Log Exception
   */
  fun exception(e: Throwable) {
    if (BuildConfig.DEBUG) {
      e.printStackTrace()
    } else {
      Crashlytics.logException(e)
    }
  }

  /**
   * Log error
   */
  fun error(e: Error) {
    if (BuildConfig.DEBUG) {
      e.printStackTrace()
    } else {
      Crashlytics.logException(e)
    }
  }
}
