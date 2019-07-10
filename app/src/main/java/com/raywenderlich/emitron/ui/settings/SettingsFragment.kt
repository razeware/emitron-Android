package com.raywenderlich.emitron.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.raywenderlich.emitron.R

class SettingsFragment : Fragment() {

  private lateinit var settingsViewModel: SettingsViewModel

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    settingsViewModel =
      ViewModelProviders.of(this).get(SettingsViewModel::class.java)
    return inflater.inflate(R.layout.fragment_settings, container, false)
  }
}
