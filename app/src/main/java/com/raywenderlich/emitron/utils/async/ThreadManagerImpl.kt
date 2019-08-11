package com.raywenderlich.emitron.utils.async

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

  override val networkIo: ExecutorService = Executors.newFixedThreadPool(5)

}
