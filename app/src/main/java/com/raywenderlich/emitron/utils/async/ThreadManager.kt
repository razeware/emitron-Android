package com.raywenderlich.emitron.utils.async

import kotlinx.coroutines.CoroutineDispatcher
import java.util.concurrent.Executor

/**
 * ThreadManager for controlling thread declaration
 * Also helps in easy mocking
 */
interface ThreadManager {
  val networkIo: Executor
  val io: CoroutineDispatcher
}
