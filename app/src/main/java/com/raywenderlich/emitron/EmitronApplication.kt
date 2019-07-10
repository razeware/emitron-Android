package com.raywenderlich.emitron

import android.app.Application
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric

class EmitronApplication : Application() {

  override fun onCreate() {
    super.onCreate()

    Fabric.with(this, Crashlytics())
  }
}
