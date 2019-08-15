package com.raywenderlich.emitron.ui.library

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.raywenderlich.emitron.MainViewModel
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.databinding.FragmentLibraryBinding
import com.raywenderlich.emitron.di.modules.viewmodel.ViewModelFactory
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.ui.common.PagedAdapter
import com.raywenderlich.emitron.ui.content.ContentAdapter
import com.raywenderlich.emitron.ui.content.ContentPagedFragment
import com.raywenderlich.emitron.utils.BottomMarginDecoration
import com.raywenderlich.emitron.utils.NetworkState
import com.raywenderlich.emitron.utils.extensions.isNetConnected
import com.raywenderlich.emitron.utils.extensions.isNetNotConnected
import com.raywenderlich.emitron.utils.extensions.observe
import com.raywenderlich.emitron.utils.extensions.setDataBindingView
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * Library view
 */
class LibraryFragment : DaggerFragment() {

  /**
   * Custom factory for viewmodel
   *
   * Custom factory provides app related dependencies
   */
  @Inject
  lateinit var viewModelFactory: ViewModelFactory

  private val viewModel: LibraryViewModel by viewModels { viewModelFactory }

  private val parentViewModel: MainViewModel by activityViewModels { viewModelFactory }

  private lateinit var binding: FragmentLibraryBinding

  private var adapter = ContentAdapter({
    openCollection(it)
  }, {
    viewModel.contentPagedViewModel.handleItemRetry(isNetConnected())
  }, {
    loadCollections()
  }, PagedAdapter())

  private val pagedFragment = lazy(LazyThreadSafetyMode.NONE) {
    ContentPagedFragment(
      viewModel.contentPagedViewModel,
      adapter
    )
  }

  /**
   * See [androidx.fragment.app.Fragment.onCreateView]
   */
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = setDataBindingView(
      inflater, R.layout.fragment_library, container
    )
    return binding.root
  }

  /**
   * See [androidx.fragment.app.Fragment.onViewCreated]
   */
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    checkLogin()
    initUi()
    initObservers()
    loadCollections()
  }

  private fun checkLogin() {
    if (!parentViewModel.isAllowed()) {
      findNavController().navigate(R.id.action_navigation_library_to_navigation_login)
    }
  }

  private fun initUi() {
    binding.textLibraryCount.text = getString(R.string.label_tutorials_count, "⋯")
    pagedFragment.value.initPaging(this, binding.recyclerViewLibrary) {
      binding.textLibraryCount.text =
        getString(R.string.label_tutorials_count, it.getTotalCount().toString())
    }

    binding.recyclerViewLibrary.addItemDecoration(BottomMarginDecoration())

    binding.buttonLibraryFilter.setOnClickListener(
      Navigation.createNavigateOnClickListener(R.id.action_navigation_library_to_navigation_filter)
    )

    binding.buttonLibrarySort.setOnClickListener {

    }
  }

  private fun initObservers() {
    viewModel.contentPagedViewModel.networkState.observe(viewLifecycleOwner) {
      handleInitialProgress(it)
    }
  }

  private fun handleInitialProgress(networkState: NetworkState?) {
    when (networkState) {
      NetworkState.INIT -> {
        showProgressView()
        binding.layoutLibraryContent.visibility = View.GONE
      }
      NetworkState.SUCCESS -> {
        hideProgressView()
        binding.layoutLibraryContent.visibility = View.VISIBLE
      }
      else -> {
        // Ignore
      }
    }
  }

  private fun loadCollections() {
    if (isNetNotConnected()) {
      pagedFragment.value.onErrorConnection()
      hideButtons()
      hideProgressView()
      return
    }

    viewModel.loadCollections()
  }

  private fun openCollection(collection: Data?) {
    collection?.let {
      val action = LibraryFragmentDirections
        .actionNavigationLibraryToNavigationCollection(collection = collection)
      findNavController().navigate(action)
    }
  }

  private fun getProgressViews() = arrayOf(
    binding.layoutLibraryProgress.layoutProgressItem1,
    binding.layoutLibraryProgress.layoutProgressItem2,
    binding.layoutLibraryProgress.layoutProgressItem3
  )

  private fun showProgressView() {
    binding.layoutLibraryProgressContainer.visibility = View.VISIBLE
    getProgressViews().map { view ->
      (view.background as? AnimationDrawable)?.let { drawable ->
        drawable.setEnterFadeDuration(500)
        drawable.setExitFadeDuration(500)
        drawable.start()
      }
    }
  }

  private fun hideProgressView() {
    val progressView = binding.layoutLibraryProgressContainer
    progressView.visibility = View.GONE
    getProgressViews().map { view ->
      (view.background as? AnimationDrawable)?.stop()
    }
  }

  private fun hideButtons() {
    binding.layoutLibraryContent.visibility = View.GONE
  }
}
