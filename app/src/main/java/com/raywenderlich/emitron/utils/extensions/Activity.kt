package com.raywenderlich.emitron.utils.extensions

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.raywenderlich.emitron.R

/**
 * File will contain all the extension functions for [Activity]s
 */

fun <T : ViewDataBinding> Activity.setDataBindingView(layoutId: Int): T =
  DataBindingUtil.setContentView(this, layoutId)

/**
 * Extension function to create and show a Snackbar
 *
 * Also customised the Snackbar ui for app
 *
 * @param text message for Snackbar
 * @param snackBarType Type of Snackbar [SnackbarType]
 */
fun AppCompatActivity.showSnackbar(
  text: String,
  snackBarType: SnackbarType = SnackbarType.Success
) {

  val anchorView = findViewById<View>(R.id.nav_view)
  val contentView = findViewById<View>(android.R.id.content)
  val snackbar = Snackbar.make(contentView, "", Snackbar.LENGTH_LONG)

  // Customise the Snackbar using an extension function
  snackbar.customiseForEmitron(text, snackBarType)

  /**
   * Anchor view might not be visible if it is [BottomNavigationView]
   * and is hidden on nested navigation levels.
   */
  if (anchorView.isVisible) {
    snackbar.anchorView = anchorView
  }
  snackbar.show()
}

/**
 * Extension function for showing Snackbar with type [SnackbarType.Warning]
 *
 *  @param text Warning message
 */
fun AppCompatActivity.showWarningSnackbar(text: String) =
  showSnackbar(text, SnackbarType.Warning)

/**
 * Extension function for showing Snackbar with type [SnackbarType.Error]
 *
 * @param text Error message
 */
fun AppCompatActivity.showErrorSnackbar(text: String) =
  showSnackbar(text, SnackbarType.Error)

/**
 * Extension function for showing Snackbar with type [SnackbarType.Success]
 *
 * @param text Success message
 */
fun AppCompatActivity.showSuccessSnackbar(text: String) =
  showSnackbar(text, SnackbarType.Success)

/**
 * Extension function for showing the Activity in fullscreen mode
 */
fun AppCompatActivity.requestGestureUi() {
  if (hasGestureUiSupport()) {
    val view = window.decorView
    view.systemUiVisibility =
      view.systemUiVisibility or
          View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
  }
}

/**
 * Check if PIP is supported on the current platform
 */
fun AppCompatActivity.hasPipSupport(): Boolean =
  Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
      packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)

/**
 * Check if notification channels are supported on the current platform
 */
fun hasNotificationChannelSupport(): Boolean =
  Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

/**
 * Check if gesture UI is supported on device
 */
fun hasGestureUiSupport(): Boolean =
  Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
