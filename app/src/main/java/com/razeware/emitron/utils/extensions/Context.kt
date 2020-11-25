package com.razeware.emitron.utils.extensions

import android.content.Context
import android.net.ConnectivityManager
import androidx.fragment.app.Fragment

/**
 * File will contain all the extension functions for [Context]s
 */

/**
 * Extension function to check connectivity
 *
 * @return Will return True if connected, otherwise False
 */
fun Context.isNetConnected(): Boolean {
  val connectivityManager =
    getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
  val networkInfo = connectivityManager.activeNetworkInfo
  return !(networkInfo == null || !networkInfo.isConnected)
}

/**
 * Extension function to check if no connectivity
 *
 * Inverse function to [Fragment.isNetConnected]
 *
 * @return Will return True if not connected, otherwise False
 */
fun Context.isNetNotConnected(): Boolean = !isNetConnected()

/**
 * Extension function to check connectivity status is metered
 *
 * @return Will return True if metered, otherwise False
 */
fun Context.isActiveNetworkMetered(): Boolean {
  val connectivityManager =
    getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
  return connectivityManager.isActiveNetworkMetered
}
