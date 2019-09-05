package com.raywenderlich.emitron.di.modules

import com.raywenderlich.emitron.BuildConfig
import com.raywenderlich.emitron.data.bookmarks.BookmarkApi
import com.raywenderlich.emitron.data.content.ContentApi
import com.raywenderlich.emitron.data.filter.FilterApi
import com.raywenderlich.emitron.data.login.LoginApi
import com.raywenderlich.emitron.data.progressions.ProgressionApi
import com.raywenderlich.emitron.network.AuthInterceptorImpl
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

@Module
class NetModule {

  @Module
  companion object {

    @JvmStatic
    @Singleton
    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
      level =
        if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.NONE
    }

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

    @Singleton
    @JvmStatic
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
      Retrofit.Builder()
        .baseUrl("https://api.raywenderlich.com/api/") // Move to BuildConfig
        .addConverterFactory(
          MoshiConverterFactory.create(
            Moshi.Builder().add(
              KotlinJsonAdapterFactory()
            ).build()
          )
        )
        .client(okHttpClient)
        .build()

    @JvmStatic
    @Provides
    fun provideLoginApi(retrofit: Retrofit): LoginApi = LoginApi.create(retrofit)

    @JvmStatic
    @Provides
    fun provideContentApi(retrofit: Retrofit): ContentApi = ContentApi.create(retrofit)

    @JvmStatic
    @Provides
    fun provideBookmarkApi(retrofit: Retrofit): BookmarkApi = BookmarkApi.create(retrofit)

    @JvmStatic
    @Provides
    fun provideProgressionApi(retrofit: Retrofit): ProgressionApi = ProgressionApi.create(retrofit)

    @JvmStatic
    @Provides
    fun provideFilterApi(retrofit: Retrofit): FilterApi = FilterApi.create(retrofit)

  }


}
