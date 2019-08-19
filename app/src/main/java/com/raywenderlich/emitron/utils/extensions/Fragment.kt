package com.raywenderlich.emitron.utils.extensions

import android.content.Context
import android.net.ConnectivityManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

/**
 * File will contain all the extension functions for [Fragment]s
 */

/**
 * Extension function to check connectivity
 *
 * @return Will return True if connected, otherwise False
 */
fun Fragment.isNetConnected(): Boolean {
  val connectivityManager =
    requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
fun Fragment.isNetNotConnected(): Boolean = !isNetConnected()

/**
 * Extension function to inflate a databinding layout from a Fragment
 *
 * Call this function from [Fragment.onCreateView] to get [ViewDataBinding]
 *
 * @param inflater LayoutInflater
 * @param layoutId Int Layout resource id
 * @param container parent layout
 *
 *
 * @return ViewDataBinding binding for layout resource
 */
fun <T : ViewDataBinding> Fragment.setDataBindingView(
  inflater: LayoutInflater,
  @LayoutRes
  layoutId: Int,
  container: ViewGroup?
): T =
  (DataBindingUtil.inflate(
    inflater,
    layoutId,
    container,
    false
  ) as T).apply {

    // Setting lifecycle owner to viewLifecycleOwner for databinding with LiveData
    lifecycleOwner = viewLifecycleOwner
  }

/**
 * Extension function for showing Snackbar with type [SnackbarType.Warning]
 *
 *  @param text Warning message
 */
fun Fragment.showWarningSnackbar(text: String) {
  (requireActivity() as? AppCompatActivity)?.showSnackbar(text, SnackbarType.Warning)
}

/**
 * Extension function for showing Snackbar with type [SnackbarType.Error]
 *
 * @param text Error message
 */
fun Fragment.showErrorSnackbar(text: String) {
  (requireActivity() as? AppCompatActivity)?.showSnackbar(text, SnackbarType.Error)
}

/**
 * Extension function for showing Snackbar with type [SnackbarType.Success]
 *
 * @param text Success message
 */
fun Fragment.showSuccessSnackbar(text: String) {
  (requireActivity() as? AppCompatActivity)?.showSnackbar(text, SnackbarType.Success)
}
