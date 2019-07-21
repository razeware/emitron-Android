package com.raywenderlich.emitron.di.modules

import com.raywenderlich.emitron.BuildConfig
import com.raywenderlich.emitron.network.AuthInterceptorImpl
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
        .addConverterFactory(MoshiConverterFactory.create())
        .client(okHttpClient)
        .build()
  }

}
