package com.raywenderlich.emitron.di.modules.worker

import android.content.Context

import androidx.work.ListenableWorker
import androidx.work.WorkerParameters

interface ChildWorkerFactory {
  fun create(appContext: Context, workerParameters: WorkerParameters): ListenableWorker
}
