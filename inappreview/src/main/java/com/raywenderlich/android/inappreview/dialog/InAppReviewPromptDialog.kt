package com.raywenderlich.android.inappreview.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.raywenderlich.android.inappreview.databinding.FragmentInAppReviewPromptBinding
import com.raywenderlich.android.inappreview.manager.InAppReviewManager
import com.raywenderlich.android.inappreview.preferences.InAppReviewPreferences
import javax.inject.Inject

/**
 * Shows a dialog that asks the user if they want to review the app.
 *
 * This dialog is shown only if the user hasn't previously rated the app, hasn't asked to never
 * rate the app or if they asked to rate it later and enough time passed (a week).
 * */
class InAppReviewPromptDialog : DialogFragment() {

  @Inject
  lateinit var preferences: InAppReviewPreferences

  @Inject
  lateinit var inAppReviewManager: InAppReviewManager

  private var binding: FragmentInAppReviewPromptBinding? = null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = FragmentInAppReviewPromptBinding.inflate(inflater, container, false)

    return binding?.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
  }

  override fun onDismiss(dialog: DialogInterface) {
    preferences.setUserChosenRateLater(true)
    preferences.setRateLater(System.currentTimeMillis())
    super.onDismiss(dialog)
  }

  override fun onCancel(dialog: DialogInterface) {
    preferences.setUserChosenRateLater(true)
    preferences.setRateLater(System.currentTimeMillis())
    super.onCancel(dialog)
  }
}