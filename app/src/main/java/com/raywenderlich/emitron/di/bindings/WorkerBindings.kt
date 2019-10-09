package com.raywenderlich.emitron.di.bindings

import com.raywenderlich.emitron.di.modules.worker.ChildWorkerFactory
import com.raywenderlich.emitron.di.modules.worker.WorkerKey
import com.raywenderlich.emitron.ui.settings.SignOutWorker
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class WorkerBindings {
  @Binds
  @IntoMap
  @WorkerKey(SignOutWorker::class)
  abstract fun bindSignOutWorker(factory: SignOutWorker.Factory): ChildWorkerFactory
}
