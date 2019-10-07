package com.raywenderlich.emitron.di.modules.worker

import androidx.work.ListenableWorker
import dagger.MapKey
import kotlin.reflect.KClass

@MapKey
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class WorkerKey(val value: KClass<out ListenableWorker>)
