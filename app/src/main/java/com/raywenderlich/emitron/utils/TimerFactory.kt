package com.raywenderlich.emitron.utils

import android.content.Context
import android.os.CountDownTimer
import android.os.Handler

fun createCountDownTimer(
  millisInFuture: Long = 1000,
  countDownInterval: Long = 1000,
  onCounterTick: ((Long) -> Unit)? = null,
  onCounterFinished: (() -> Unit)?
): CountDownTimer {
  return object : CountDownTimer(millisInFuture, countDownInterval) {

    override fun onTick(millisUntilFinished: Long) {
      onCounterTick?.invoke(millisUntilFinished)
    }

    override fun onFinish() {
      onCounterFinished?.invoke()
    }
  }
}

fun createMainThreadScheduledHandler(
  context: Context,
  interval: Long = 1000,
  block: () -> Unit
): Handler {
  val handler = Handler(context.mainLooper)
  handler.postDelayed(object : Runnable {
    override fun run() {
      block()
      handler.postDelayed(this, interval)
    }
  }, interval)
  return handler
}
