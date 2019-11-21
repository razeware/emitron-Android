package com.razeware.emitron.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.razeware.emitron.BuildConfig
import com.razeware.emitron.R

/**
 * Utility class for app support
 */
object Support {

  private const val supportEmails = "support@razeware.com"

  /**
   * Share App
   */
  fun shareApp(ctx: Context) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
      type = "text/plain";//NON-NLS
      putExtra(Intent.EXTRA_SUBJECT, ctx.getString(R.string.share_app_title))
      putExtra(Intent.EXTRA_TEXT, ctx.getString(R.string.share_app_body))
    }
    ctx.startActivity(shareIntent)
  }

  /**
   * Send Feedback
   */
  fun sendFeedback(ctx: Context) {
    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
      data = Uri.parse("mailto:")
      putExtra(Intent.EXTRA_EMAIL, supportEmails)
      putExtra(Intent.EXTRA_SUBJECT, ctx.getString(R.string.title_feedback_email))
      putExtra(Intent.EXTRA_TEXT, getDeviceDetails())
    }
    ctx.startActivity(
      Intent.createChooser(
        emailIntent,
        ctx.getString(R.string.label_send_feedback)
      )
    )
  }

  private fun getDeviceDetails(): String =
    """
      App Version: ${BuildConfig.VERSION_NAME}, ${BuildConfig.VERSION_CODE}
      Device Info: ${Build.VERSION.RELEASE}, ${Build.MODEL}
    """.trimIndent()
}
