package com.razeware.emitron.di.modules

import com.razeware.emitron.BuildConfig
import com.razeware.emitron.data.bookmarks.BookmarkApi
import com.razeware.emitron.data.content.ContentApi
import com.razeware.emitron.data.download.DownloadApi
import com.razeware.emitron.data.filter.FilterApi
import com.razeware.emitron.data.login.LoginApi
import com.razeware.emitron.data.progressions.ProgressionApi
import com.razeware.emitron.data.video.VideoApi
import com.razeware.emitron.network.AuthInterceptorImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Dagger module for network ops
 */
@Module
@InstallIn(ApplicationComponent::class)
class NetModule {

  /**
   * Provide logging interceptor
   */
  @Singleton
  @Provides
  fun provideLoggingInterceptor(): HttpLoggingInterceptor =
    HttpLoggingInterceptor().apply {
      level = if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor.Level.HEADERS
      } else {
        HttpLoggingInterceptor.Level.NONE
      }
    }

  /**
   * Provide OkHttp
   */
  @Singleton
  @Provides
  fun provideOkHttp(
    loggingInterceptor: HttpLoggingInterceptor,
    authInterceptor: AuthInterceptorImpl
  ): OkHttpClient =
    OkHttpClient.Builder()
      .connectTimeout(30, TimeUnit.SECONDS)
      .callTimeout(30, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .writeTimeout(30, TimeUnit.SECONDS)
      .addNetworkInterceptor(authInterceptor)
      .addInterceptor(loggingInterceptor)
      .build()

  /**
   * Provide Retrofit
   */
  @Singleton
  @Provides
  fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
    Retrofit.Builder()
      .baseUrl(BuildConfig.BASE_API_URL)
      .addConverterFactory(
        MoshiConverterFactory.create(
          Moshi.Builder().add(
            KotlinJsonAdapterFactory()
          ).build()
        )
      )
      .client(okHttpClient)
      .build()

  /**
   * Create [LoginApi]
   */
  @Provides
  fun provideLoginApi(retrofit: Retrofit): LoginApi = LoginApi.create(retrofit)

  /**
   * Create [ContentApi]
   */
  @Provides
  fun provideContentApi(retrofit: Retrofit): ContentApi = ContentApi.create(retrofit)

  /**
   * Create [BookmarkApi]
   */
  @Provides
  fun provideBookmarkApi(retrofit: Retrofit): BookmarkApi = BookmarkApi.create(retrofit)

  /**
   * Create [ProgressionApi]
   */
  @Provides
  fun provideProgressionApi(retrofit: Retrofit): ProgressionApi = ProgressionApi.create(retrofit)

  /**
   * Create [FilterApi]
   */
  @Provides
  fun provideFilterApi(retrofit: Retrofit): FilterApi = FilterApi.create(retrofit)

  /**
   * Create [VideoApi]
   */
  @Provides
  fun provideVideoApi(retrofit: Retrofit): VideoApi = VideoApi.create(retrofit)

  /**
   * Create [DownloadApi]
   */
  @Provides
  fun provideDownloadApi(retrofit: Retrofit): DownloadApi = DownloadApi.create(retrofit)
}
