package com.razeware.emitron.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.razeware.emitron.BuildConfig
import com.razeware.emitron.data.settings.SettingsRepository
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
class LoggerImpl @Inject constructor(private val settingsRepository: SettingsRepository) : Logger {
  override fun log(e: Throwable) {
    if (settingsRepository.isCrashReportingAllowed()) {
      com.razeware.emitron.utils.Log.exception(e)
    }
  }

  override fun log(e: Error) {
    if (settingsRepository.isCrashReportingAllowed()) {
      com.razeware.emitron.utils.Log.error(e)
    }
  }
}

/**
 * Log Utils
 */
internal object Log {

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
      FirebaseCrashlytics.getInstance().recordException(e)
    }
  }

  /**
   * Log error
   */
  fun error(e: Error) {
    if (BuildConfig.DEBUG) {
      e.printStackTrace()
    } else {
      FirebaseCrashlytics.getInstance().recordException(e)
    }
  }
}
