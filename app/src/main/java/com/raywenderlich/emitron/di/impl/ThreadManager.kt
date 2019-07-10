package com.raywenderlich.emitron.di.impl

import kotlinx.coroutines.CoroutineDispatcher
import java.util.concurrent.Executor

interface ThreadManager {
  val networkIo: Executor
  val io: CoroutineDispatcher
}
