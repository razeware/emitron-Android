package com.razeware.emitron.utils.async

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

/**
 * ThreadManager Implementation
 *
 */
class ThreadManagerImpl @Inject constructor() :
  ThreadManager {

  override val io: CoroutineDispatcher = Dispatchers.IO

  override val db: CoroutineDispatcher = Dispatchers.IO

  override val networkExecutor: ExecutorService = Executors.newFixedThreadPool(5)

  override val dbExecutor: ExecutorService = Executors.newFixedThreadPool(5)

}
