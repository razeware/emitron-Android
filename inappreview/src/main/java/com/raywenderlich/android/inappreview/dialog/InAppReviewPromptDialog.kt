package com.raywenderlich.android.inappreview.dialog

import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.DialogFragment
import com.raywenderlich.android.inappreview.R
import com.raywenderlich.android.inappreview.databinding.FragmentInAppReviewPromptBinding
import com.raywenderlich.android.inappreview.manager.InAppReviewManager
import com.raywenderlich.android.inappreview.preferences.InAppReviewPreferences
import com.raywenderlich.android.preferences.GeneralSettingsPrefs
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/**
 * Shows a dialog that asks the user if they want to review the app.
 *
 * This dialog is shown only if the user hasn't previously rated the app, hasn't asked to never
 * rate the app or if they asked to rate it later and enough time passed (a week).
 * */
@AndroidEntryPoint
class InAppReviewPromptDialog : DialogFragment() {

  /**
   * Preferences used to update the rate app prompt flags.
   * */
  @Inject
  lateinit var preferences: InAppReviewPreferences

  @Inject
  lateinit var generalSettingsPrefs: GeneralSettingsPrefs

  /**
   * Manager used to trigger the In App Review prompt if needed.
   * */
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
    initListeners()
    dialog?.setCanceledOnTouchOutside(false)
  }

  private fun initListeners() {
    val binding = binding ?: return

    binding.leaveReview.setOnClickListener { onLeaveReviewTapped() }
    binding.reviewLater.setOnClickListener { onRateLaterTapped() }
  }

  private fun onLeaveReviewTapped() {
    preferences.setUserRatedApp(true)
    inAppReviewManager.startReview(requireActivity())
    dismissAllowingStateLoss()
  }

  private fun onRateLaterTapped() {
    preferences.setUserChosenRateLater(true)
    preferences.setRateLater(getLaterTime())
    dismissAllowingStateLoss()
  }

  /**
   * Styles the dialog to have a transparent background and window insets.
   * */
  override fun onStart() {
    super.onStart()
    initStyle()
  }

  private fun initStyle() {
    val back = ColorDrawable(Color.TRANSPARENT)
    dialog?.window?.setBackgroundDrawable(back)

    dialog?.window?.setLayout(
      resources.getDimensionPixelSize(R.dimen.ratePromptWidth),
      resources.getDimensionPixelSize(R.dimen.ratePromptHeight)
    )

    val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    val modePreferences = generalSettingsPrefs.getNightMode()

    val isInNightMode =
      (modePreferences == AppCompatDelegate.MODE_NIGHT_YES
          || (modePreferences == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
          && currentNightMode == Configuration.UI_MODE_NIGHT_YES))

    binding?.progressBar?.setImageResource(
      if (isInNightMode) R.drawable.progress_white else R.drawable.progress_black
    )
  }

  /**
   * If the user cancels the dialog, we process that as if they chose to "Rate Later".
   * */
  override fun onCancel(dialog: DialogInterface) {
    preferences.setUserChosenRateLater(true)
    preferences.setRateLater(getLaterTime())
    super.onCancel(dialog)
  }

  private fun getLaterTime(): Long {
    return System.currentTimeMillis() + TimeUnit.DAYS.toMillis(14)
  }
}