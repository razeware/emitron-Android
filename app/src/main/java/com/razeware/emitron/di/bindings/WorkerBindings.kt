package com.razeware.emitron.di.bindings

import com.razeware.emitron.ui.download.workers.*
import com.razeware.emitron.ui.player.workers.UpdateOfflineProgressWorker
import com.razeware.emitron.ui.settings.SignOutWorker
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class WorkerBindings {

  @ContributesAndroidInjector
  abstract fun bindSignOutWorker(): SignOutWorker

  @ContributesAndroidInjector
  abstract fun bindStartDownloadWorker(): StartDownloadWorker

  @ContributesAndroidInjector
  abstract fun bindDownloadWorker(): DownloadWorker

  @ContributesAndroidInjector
  abstract fun bindUpdateDownloadWorker(): UpdateDownloadWorker

  @ContributesAndroidInjector
  abstract fun bindRemoveWorker(): RemoveDownloadWorker

  @ContributesAndroidInjector
  abstract fun bindVerifyDownloadWorker(): VerifyDownloadWorker

  @ContributesAndroidInjector
  abstract fun bindUpdateOfflineProgressWorker(): UpdateOfflineProgressWorker

  @ContributesAndroidInjector
  abstract fun bindPendingDownloadWorker(): PendingDownloadWorker

}
