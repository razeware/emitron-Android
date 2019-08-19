package com.raywenderlich.emitron.ui.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.databinding.FragmentFilterBinding
import com.raywenderlich.emitron.di.modules.viewmodel.ViewModelFactory
import com.raywenderlich.emitron.utils.extensions.setDataBindingView
import com.raywenderlich.emitron.utils.getDefaultAppBarConfiguration
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class FilterFragment : DaggerFragment() {

  @Inject
  lateinit var viewModelFactory: ViewModelFactory

  private val viewModel: FilterViewModel by viewModels { viewModelFactory }

  private lateinit var binding: FragmentFilterBinding

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = setDataBindingView(inflater, R.layout.fragment_filter, container)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    ViewCompat.setTranslationZ(view, 100f)
    initView()
  }

  private fun initView() {
    binding.toolbar.setupWithNavController(findNavController(), getDefaultAppBarConfiguration())
    binding.toolbar.navigationIcon =
      VectorDrawableCompat.create(resources, R.drawable.ic_material_icon_close, null)
  }
}
