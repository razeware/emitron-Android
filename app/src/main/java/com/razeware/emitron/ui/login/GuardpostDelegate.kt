package com.razeware.emitron.ui.login

import android.content.Context
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.raywenderlich.guardpost.GuardpostAuth
import com.raywenderlich.guardpost.GuardpostAuthReceiver
import com.raywenderlich.guardpost.utils.randomString
import com.razeware.emitron.R

class GuardpostDelegate(private val context: Context) {

  private val localBroadcastManager by lazy(LazyThreadSafetyMode.NONE) {
    LocalBroadcastManager.getInstance(context)
  }

  private val guardpostAuthReceiver = GuardpostAuthReceiver()

  fun registerReceiver(): GuardpostAuthReceiver {
    localBroadcastManager.registerReceiver(
      guardpostAuthReceiver,
      GuardpostAuth.BroadcastActions.INTENT_FILTER
    )
    return guardpostAuthReceiver
  }

  fun login() {
    val clientApiKey = context.getString(R.string.client_api_key)
    val nonce = randomString()

    context.let {
      GuardpostAuth.startLogin(it, clientApiKey, nonce)
    }
  }

  fun logout() {
    GuardpostAuth.startLogout(context)
  }

  fun clear() {
    localBroadcastManager.unregisterReceiver(guardpostAuthReceiver)
  }
}
