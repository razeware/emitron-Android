package com.razeware.emitron.utils

import android.content.Context
import android.os.CountDownTimer
import android.os.Handler

/**
 * Create a count down timer
 *
 * @param millisInFuture count down duration
 * @param countDownInterval count down tick interval
 * @param onCounterTick callback on each counter tick
 * @param onCounterFinished callback on counter end
 */
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

/**
 * Create a handler which runs on main looper
 *
 * @param interval interval for handler
 * @param block block to execute after each interval
 */
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
