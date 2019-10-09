package com.raywenderlich.emitron.di.modules.worker

import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module

@Module(includes = [AssistedInject_AssistedWorkerModule::class])
@AssistedModule
interface AssistedWorkerModule
