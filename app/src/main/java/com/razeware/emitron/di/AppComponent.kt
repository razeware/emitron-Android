package com.razeware.emitron.di

import android.app.Application
import com.razeware.emitron.EmitronApplication
import com.razeware.emitron.di.modules.AppModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(
  modules = [
    AppModule::class,
    AppBindings::class
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
