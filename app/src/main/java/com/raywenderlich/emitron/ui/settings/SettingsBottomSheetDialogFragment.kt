package com.raywenderlich.emitron.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.databinding.FragmentSettingsBottomsheetBinding
import com.raywenderlich.emitron.di.modules.viewmodel.ViewModelFactory
import com.raywenderlich.emitron.utils.extensions.setDataBindingView
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * Settings UIr
 */
class SettingsBottomSheetDialogFragment : BottomSheetDialogFragment() {

  companion object {

    /**
     * Create and show
     *
     * @param navController NavController used to show
     * @param headerResId Res id for header
     */
    fun show(
      navController: NavController,
      @StringRes headerResId: Int
    ) {
      val settingsBottomSheetDirection =
        SettingsFragmentDirections
          .actionNavigationSettingsBottomSheet(headerResId)
      navController.navigate(settingsBottomSheetDirection)
    }
  }

  /**
   * Custom factory for viewmodel
   *
   * Custom factory provides app related dependencies
   */
  @Inject
  lateinit var viewModelFactory: ViewModelFactory

  private lateinit var viewBinding: FragmentSettingsBottomsheetBinding

  private val args by navArgs<SettingsBottomSheetDialogFragmentArgs>()

  private val viewModel:
      SettingsViewModel by navGraphViewModels(R.id.settings_navigation) { viewModelFactory }

  /**
   * See [androidx.fragment.app.Fragment.onCreateView]
   */
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    viewBinding = setDataBindingView(inflater, R.layout.fragment_settings_bottomsheet, container)
    return viewBinding.root
  }

  /**
   * See [androidx.fragment.app.Fragment.onViewCreated]
   */
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val headerResId = args.header
    if (headerResId != 0) {
      viewBinding.titleSettingsBottomSheet.text = getString(headerResId)
    }
    with(viewBinding.recyclerView) {
      layoutManager = object : LinearLayoutManager(requireContext()) {
        override fun canScrollVertically(): Boolean = false
      }
      adapter = SettingsBottomSheetAdapter(getSettingsOptions(headerResId)) {
        onSettingsChange(headerResId, it)
      }
    }
  }

  /**
   * See [androidx.fragment.app.Fragment.onAttach]
   */
  override fun onAttach(context: Context) {
    AndroidSupportInjection.inject(this)
    super.onAttach(context)
  }

  private fun getSettingsOptions(@StringRes headerResId: Int): List<String> {
    return when (headerResId) {
      R.string.label_night_mode -> {
        getNightModeOptions().map { getString(it) }
      }
      else -> {
        getNightModeOptions().map { getString(it) }
      }
    }
  }

  // The order of options will define the order in options UI
  private fun getNightModeOptions(): List<Int> =
    listOf(
      R.string.button_on,
      R.string.button_off,
      R.string.button_system_default
    )

  private fun onSettingsChange(
    @StringRes headerResId: Int,
    position: Int
  ) {
    when (headerResId) {
      R.string.label_night_mode -> {
        updateNightModeSettings(position, getNightModeOptions())
      }
    }
  }

  /**
   * @param position selected settings option
   *
   */
  private fun updateNightModeSettings(position: Int, options: List<Int>) {
    val nightMode = when (options[position]) {
      R.string.button_off -> {
        AppCompatDelegate.MODE_NIGHT_NO
      }
      R.string.button_on -> {
        AppCompatDelegate.MODE_NIGHT_YES
      }
      else -> {
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
      }
    }
    viewModel.updateNightMode(nightMode)
    dismiss()
  }
}
