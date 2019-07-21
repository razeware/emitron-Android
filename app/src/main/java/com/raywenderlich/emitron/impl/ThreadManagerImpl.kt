package com.raywenderlich.emitron.impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.Executors
import javax.inject.Inject

class ThreadManagerImpl @Inject constructor() : ThreadManager {

  override val io: CoroutineDispatcher = Dispatchers.IO

  override val networkIo = Executors.newFixedThreadPool(5)

}
