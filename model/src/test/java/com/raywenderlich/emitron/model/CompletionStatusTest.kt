package com.raywenderlich.emitron.model

import com.raywenderlich.emitron.model.utils.isEqualTo
import org.junit.Test

class CompletionStatusTest {

  @Test
  fun isCompleted() {
    val completionStatus = CompletionStatus.Completed
    completionStatus.isCompleted() isEqualTo true
    val completionStatusInProgress = CompletionStatus.InProgress
    completionStatusInProgress.isCompleted() isEqualTo false
  }
}
