package com.razeware.emitron.di.modules

import android.app.Application
import android.content.Context
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.razeware.emitron.BuildConfig
import com.razeware.emitron.ui.download.DownloadService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

private const val DOWNLOAD_CONTENT_DIRECTORY = "downloads"

@Module
@InstallIn(SingletonComponent::class)
class DownloadModule {

  @Provides
  @Singleton
  fun provideSimpleCache(
    application: Application,
    databaseProvider: DatabaseProvider
  ): Cache {
    val downloadDirectory = getDownloadDirectory(application)
    return SimpleCache(downloadDirectory, NoOpCacheEvictor(), databaseProvider)
  }

  @Provides
  @Singleton
  fun provideExoDatabaseProvider(application: Application): DatabaseProvider =
    StandaloneDatabaseProvider(application)

  @Provides
  @Singleton
  fun provideExoDownloadManager(
    application: Application,
    databaseProvider: DatabaseProvider,
    cache: Cache
  ): DownloadManager =
    buildDownloadManager(application, databaseProvider, cache)

  internal fun getDownloadDirectory(context: Context): File {
    var downloadDirectory = context.getExternalFilesDir(null)
    if (downloadDirectory == null) {
      downloadDirectory = context.filesDir
    }
    return File(downloadDirectory, DOWNLOAD_CONTENT_DIRECTORY)
  }

  internal fun buildDownloadManager(
    context: Context,
    databaseProvider: DatabaseProvider,
    downloadCache: Cache
  ): DownloadManager {

    return DownloadManager(
      context,
      databaseProvider,
      downloadCache,
      DownloadService.buildHttpDataSourceFactory(BuildConfig.APPLICATION_ID),
      Runnable::run
    )
  }
}
