package com.razeware.emitron.utils.extensions

import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.core.view.updatePadding
import androidx.core.view.updatePaddingRelative
import com.google.android.material.snackbar.Snackbar
import com.razeware.emitron.R
import com.razeware.emitron.databinding.LayoutSnackbarDefaultBinding

/**
 * Custom Snackbar types
 */
enum class SnackbarType {
  /**
   * Color error
   */
  Error,
  /**
   * Color warning
   */
  Warning,
  /**
   * Color primary
   */
  Success
}

/**
 * Extension function to customise Snackbar layout for emitron
 */
fun Snackbar.customiseForEmitron(
  text: String,
  snackbarType: SnackbarType
) {
  val context = context
  val snackView: LayoutSnackbarDefaultBinding =
    LayoutInflater.from(context)
      .inflateDatabindingLayout(R.layout.layout_snackbar_default)

  val layout = view as? Snackbar.SnackbarLayout
  val getColor = { colorResId: Int ->
    ContextCompat.getColor(
      context,
      colorResId
    )
  }

  with(snackView) {
    buttonSnackbarDismiss.setOnClickListener {
      dismiss()
    }
    textSnackbarBody.text = text

    val updateSnackbarUi: (color: Int, message: String) -> Unit = { color, message ->
      snackbarCard.setCardBackgroundColor(color)
      textSnackbarType.setTextColor(color)
      textSnackbarType.text = message
    }

    when (snackbarType) {
      SnackbarType.Error -> {
        updateSnackbarUi(
          getColor(R.color.colorError),
          context.getString(R.string.snackbar_label_error)
        )
      }
      SnackbarType.Warning -> {
        updateSnackbarUi(
          getColor(R.color.colorWarning),
          context.getString(R.string.snackbar_label_warning)
        )
      }
      SnackbarType.Success -> {
        updateSnackbarUi(
          getColor(R.color.colorPrimary),
          context.getString(R.string.snackbar_label_success)
        )
      }
    }

  }

  layout?.apply {
    layout.setBackgroundColor(getColor(R.color.transparent))
    removeAllViews()
    setPadding(0)
    updatePaddingRelative(0, 0, 0, 0)
    updatePadding(0, 0, 0, 0)
    addView(snackView.root, 0)
  }
}
