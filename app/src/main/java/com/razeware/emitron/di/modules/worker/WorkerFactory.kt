package com.razeware.emitron.di.modules.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import javax.inject.Inject
import javax.inject.Provider

class WorkerFactory @Inject constructor(
  private val creators: Map<Class<out ListenableWorker>, @JvmSuppressWildcards Provider<ChildWorkerFactory>>
) : WorkerFactory() {

  override fun createWorker(
    appContext: Context,
    workerClassName: String,
    workerParameters: WorkerParameters
  ): ListenableWorker? {
    val workerClass = Class.forName(workerClassName)
    val foundEntry =
      creators.entries.find { workerClass.isAssignableFrom(it.key) }
    return if (foundEntry != null) {
      val factoryProvider = foundEntry.value
      factoryProvider.get().create(appContext, workerParameters)
    } else {
      null
    }
  }
}
