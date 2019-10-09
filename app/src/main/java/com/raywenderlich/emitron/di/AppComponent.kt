package com.raywenderlich.emitron.di

import android.app.Application
import com.raywenderlich.emitron.EmitronApplication
import com.raywenderlich.emitron.di.modules.AppModule
import com.raywenderlich.emitron.di.modules.worker.AssistedWorkerModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(
  modules = [
    AppModule::class,
    AppBindings::class,
    AssistedWorkerModule::class
  ]
)
interface AppComponent : AndroidInjector<EmitronApplication> {
  fun inject(app: Application)

  @Component.Builder
  interface Builder {
    @BindsInstance
    fun app(application: Application): Builder

    fun build(): AppComponent
  }
}
