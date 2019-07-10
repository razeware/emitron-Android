package com.raywenderlich.emitron.utils.extensions

import android.content.Context
import android.net.ConnectivityManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders


inline fun <reified T : ViewModel> Fragment.createViewModel(viewModelFactory: ViewModelProvider.Factory): T =
  ViewModelProviders.of(this, viewModelFactory)[T::class.java]

inline fun <reified T : ViewModel> Fragment.createActivityViewModel(viewModelFactory: ViewModelProvider.Factory): T =
  ViewModelProviders.of(this.activity!!, viewModelFactory)[T::class.java]

fun Fragment.isNetConnected(): Boolean {
  val connectivityManager =
    context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
  val networkInfo = connectivityManager.activeNetworkInfo
  return !(networkInfo == null || !networkInfo.isConnectedOrConnecting)
}

fun Fragment.isNetNotConnected() = !isNetConnected()
