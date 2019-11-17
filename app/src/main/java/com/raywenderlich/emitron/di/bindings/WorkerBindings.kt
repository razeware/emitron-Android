package com.raywenderlich.emitron.di.bindings

import com.raywenderlich.emitron.di.modules.worker.ChildWorkerFactory
import com.raywenderlich.emitron.di.modules.worker.WorkerKey
import com.raywenderlich.emitron.ui.download.workers.*
import com.raywenderlich.emitron.ui.player.workers.UpdateOfflineProgressWorker
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

  @Binds
  @IntoMap
  @WorkerKey(StartDownloadWorker::class)
  abstract fun bindStartDownloadWorker(factory: StartDownloadWorker.Factory): ChildWorkerFactory

  @Binds
  @IntoMap
  @WorkerKey(DownloadWorker::class)
  abstract fun bindDownloadWorker(factory: DownloadWorker.Factory): ChildWorkerFactory

  @Binds
  @IntoMap
  @WorkerKey(UpdateDownloadWorker::class)
  abstract fun bindUpdateDownloadWorker(factory: UpdateDownloadWorker.Factory): ChildWorkerFactory

  @Binds
  @IntoMap
  @WorkerKey(RemoveDownloadWorker::class)
  abstract fun bindRemoveWorker(factory: RemoveDownloadWorker.Factory): ChildWorkerFactory

  @Binds
  @IntoMap
  @WorkerKey(VerifyDownloadWorker::class)
  abstract fun bindVerifyDownloadWorker(factory: VerifyDownloadWorker.Factory): ChildWorkerFactory

  @Binds
  @IntoMap
  @WorkerKey(UpdateOfflineProgressWorker::class)
  abstract fun bindUpdateOfflineProgressWorker(factory: UpdateOfflineProgressWorker.Factory): ChildWorkerFactory

  @Binds
  @IntoMap
  @WorkerKey(PendingDownloadWorker::class)
  abstract fun bindPendingDownloadWorker(factory: PendingDownloadWorker.Factory): ChildWorkerFactory

}
