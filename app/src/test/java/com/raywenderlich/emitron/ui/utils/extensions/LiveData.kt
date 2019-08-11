package com.raywenderlich.emitron.ui.utils.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import com.nhaarman.mockitokotlin2.mock

class TestObserver<T> : Observer<T> {
  var value: T? = null

  override fun onChanged(t: T?) {
    this.value = t
  }
}

fun <T> LiveData<T>?.observeForTestingResult(): T {
  val testObserver: TestObserver<T> = TestObserver()
  this?.observeForever(testObserver)
  return testObserver.value!!
}

fun <T> LiveData<T>?.observeForTestingResultNullable(): T? {
  val testObserver: TestObserver<T> = TestObserver()
  this?.observeForever(testObserver)
  return testObserver.value
}

fun <T> LiveData<T>?.observeForTestingObserver(): TestObserver<T> {
  val testObserver: TestObserver<T> = mock()
  this?.observeForever(testObserver)
  return testObserver
}

fun <T> PagedList<T>.loadAllData() {
  do {
    val oldSize = this.loadedCount
    this.loadAround(this.size - 1)
  } while (this.size != oldSize)
}
