package com.raywenderlich.emitron.ui.collection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.databinding.FragmentCollectionBinding
import com.raywenderlich.emitron.di.modules.viewmodel.ViewModelFactory
import com.raywenderlich.emitron.utils.extensions.setDataBindingView
import com.raywenderlich.emitron.utils.getDefaultAppBarConfiguration
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class CollectionFragment : DaggerFragment() {

  @Inject
  lateinit var viewModelFactory: ViewModelFactory

  private val viewModel: CollectionViewModel by viewModels { viewModelFactory }

  private lateinit var binding: FragmentCollectionBinding

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = setDataBindingView(
      inflater,
      R.layout.fragment_collection,
      container
    )
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initView()
  }

  private fun initView() {
    binding.toolbar.setupWithNavController(findNavController(), getDefaultAppBarConfiguration())
  }
}
