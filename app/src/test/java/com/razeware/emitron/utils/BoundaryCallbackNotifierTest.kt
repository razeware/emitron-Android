package com.razeware.emitron.utils

import org.junit.Test

class BoundaryCallbackNotifierTest {

  @Test
  fun incrementAndDecrement() {
    // When
    val boundaryCallbackNotifier1 = BoundaryCallbackNotifier()
    boundaryCallbackNotifier1.decrement()

    // Assertions
    boundaryCallbackNotifier1.requestCount isEqualTo 0
    boundaryCallbackNotifier1.hasRequests() isEqualTo false

    // When
    val boundaryCallbackNotifier2 = BoundaryCallbackNotifier()

    // Assertions
    boundaryCallbackNotifier2.requestCount isEqualTo 0
    boundaryCallbackNotifier2.hasRequests() isEqualTo false

    // When
    val boundaryCallbackNotifier3 = BoundaryCallbackNotifier()
    boundaryCallbackNotifier3.increment()

    // Assertions
    boundaryCallbackNotifier3.requestCount isEqualTo 1
    boundaryCallbackNotifier3.hasRequests() isEqualTo true

    boundaryCallbackNotifier3.decrement()

    // Assertions
    boundaryCallbackNotifier3.requestCount isEqualTo 0
    boundaryCallbackNotifier3.hasRequests() isEqualTo false
  }

  @Test
  fun pageReset() {
    // When
    val boundaryCallbackNotifier1 = BoundaryCallbackNotifier()
    boundaryCallbackNotifier1.reset(true)

    // Assertions
    boundaryCallbackNotifier1.shouldReset() isEqualTo true
    boundaryCallbackNotifier1.shouldReset() isEqualTo false
  }
}
