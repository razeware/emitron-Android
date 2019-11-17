package com.razeware.emitron.utils.async

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
  val networkExecutor: Executor
  /**
   * IO dispatched for coroutine(s)
   */
  val io: CoroutineDispatcher

  /**
   * Default database executor
   */
  val dbExecutor: Executor

  /**
   * IO dispatched for coroutine(s)
   */
  val db: CoroutineDispatcher
}
