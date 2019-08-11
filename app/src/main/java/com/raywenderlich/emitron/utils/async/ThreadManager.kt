package com.raywenderlich.emitron.utils.async

import kotlinx.coroutines.CoroutineDispatcher
import java.util.concurrent.Executor

/**
 * ThreadManager for controlling thread usage
 *
 * Helpful in mocking and testing
 */
interface ThreadManager {
  /**
   * Default network executor
   */
  val networkIo: Executor
  /**
   * IO dispatched for coroutine(s)
   */
  val io: CoroutineDispatcher
}
