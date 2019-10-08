package com.raywenderlich.emitron.utils.extensions

import android.content.Context
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
fun Fragment.isNetConnected(): Boolean = requireContext().isNetConnected()

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
  @LayoutRes
  layoutId: Int,
  container: ViewGroup?
): T =
  (DataBindingUtil.inflate(
    layoutInflater,
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

fun Fragment.hideKeyboard() {
  val inputMethodManager =
    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  if (view != null) {
    inputMethodManager.hideSoftInputFromWindow(requireView().windowToken, 0)
  }
  if (view != null) {
    requireView().clearFocus()
  }
}

fun Fragment.requestLandscapeOrientation(isLandscape: Boolean = true) {
  requireActivity().requestedOrientation = if (isLandscape) {
    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
  } else {
    ActivityInfo.SCREEN_ORIENTATION_USER
  }
}
