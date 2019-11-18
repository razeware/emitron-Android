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
class NetModule {

  @Module
  companion object {

    /**
     * Provide logging interceptor
     */
    @JvmStatic
    @Singleton
    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
      level =
        if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.NONE
    }

    /**
     * Provide OkHttp
     */
    @JvmStatic
    @Singleton
    @Provides
    fun provideOkHttp(
      loggingInterceptor: HttpLoggingInterceptor,
      authInterceptor: AuthInterceptorImpl
    ): OkHttpClient =
      OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .addNetworkInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    /**
     * Provide Retrofit
     */
    @Singleton
    @JvmStatic
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
      Retrofit.Builder()
        .baseUrl("https://api.razeware.com/api/") // Move to BuildConfig
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
    @JvmStatic
    @Provides
    fun provideLoginApi(retrofit: Retrofit): LoginApi = LoginApi.create(retrofit)

    /**
     * Create [ContentApi]
     */
    @JvmStatic
    @Provides
    fun provideContentApi(retrofit: Retrofit): ContentApi = ContentApi.create(retrofit)

    /**
     * Create [BookmarkApi]
     */
    @JvmStatic
    @Provides
    fun provideBookmarkApi(retrofit: Retrofit): BookmarkApi = BookmarkApi.create(retrofit)

    /**
     * Create [ProgressionApi]
     */
    @JvmStatic
    @Provides
    fun provideProgressionApi(retrofit: Retrofit): ProgressionApi = ProgressionApi.create(retrofit)

    /**
     * Create [FilterApi]
     */
    @JvmStatic
    @Provides
    fun provideFilterApi(retrofit: Retrofit): FilterApi = FilterApi.create(retrofit)

    /**
     * Create [VideoApi]
     */
    @JvmStatic
    @Provides
    fun provideVideoApi(retrofit: Retrofit): VideoApi = VideoApi.create(retrofit)

    /**
     * Create [DownloadApi]
     */
    @JvmStatic
    @Provides
    fun provideDownloadApi(retrofit: Retrofit): DownloadApi = DownloadApi.create(retrofit)

  }


}
