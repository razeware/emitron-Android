package com.raywenderlich.emitron.ui.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.raywenderlich.emitron.MainViewModel
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.databinding.FragmentFilterBinding
import com.raywenderlich.emitron.di.modules.viewmodel.ViewModelFactory
import com.raywenderlich.emitron.utils.extensions.observe
import com.raywenderlich.emitron.utils.extensions.setDataBindingView
import com.raywenderlich.emitron.utils.extensions.showErrorSnackbar
import com.raywenderlich.emitron.utils.getDefaultAppBarConfiguration
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_filter.*
import javax.inject.Inject

/**
 * Filter view
 */
class FilterFragment : DaggerFragment() {

  /**
   * Custom factory for viewmodel
   *
   * Custom factory provides app related dependencies
   */
  @Inject
  lateinit var viewModelFactory: ViewModelFactory

  private val viewModel: FilterViewModel by viewModels { viewModelFactory }

  private val parentViewModel: MainViewModel by activityViewModels { viewModelFactory }

  private val filterAdapter by lazy(LazyThreadSafetyMode.NONE) { createFilterAdapter() }

  private lateinit var binding: FragmentFilterBinding

  /**
   * See [androidx.fragment.app.Fragment.onCreateView]
   */
  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {
    binding = setDataBindingView(inflater, R.layout.fragment_filter, container)
    return binding.root
  }

  /**
   * See [androidx.fragment.app.Fragment.onViewCreated]
   */
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    ViewCompat.setTranslationZ(view, 100f)
    initToolbar()
    initUi()
    loadFilters()
  }

  private fun initToolbar() {
    binding.toolbar.setupWithNavController(findNavController(), getDefaultAppBarConfiguration())
    binding.toolbar.navigationIcon =
        VectorDrawableCompat.create(resources, R.drawable.ic_material_icon_close, null)
  }

  private fun initUi() {
    binding.buttonFilterClose.setOnClickListener {
      parentViewModel.setSelectedFilter(filterAdapter.getSelectedOptions())
      findNavController().navigate(R.id.action_navigation_filter_to_navigation_library)
    }
    binding.buttonFilterClear.setOnClickListener {
      parentViewModel.setSelectedFilter(emptyList())
      findNavController().navigate(R.id.action_navigation_filter_to_navigation_library)
    }

    with(recycler_view_filter) {
      layoutManager = LinearLayoutManager(context)
      setHasFixedSize(true)
      adapter = filterAdapter
    }
    viewModel.loadFilterOptionsResult.observe(viewLifecycleOwner) {

      when (it?.getContentIfNotHandled()) {
        FilterViewModel.LoadFilterOptionResult.FailedToFetchDomains -> {
          if (!viewModel.hasDomains()) {
            showErrorSnackbar(getString(R.string.error_load_failed_domains))
          }
        }
        FilterViewModel.LoadFilterOptionResult.FailedToFetchCategories ->
          if (!viewModel.hasCategories()) {
            showErrorSnackbar(getString(R.string.error_load_failed_categories))
          }
        null -> {
          // Houston, We Have a Problem!
        }
      }
    }
  }

  private fun createFilterAdapter(): FilterAdapter {
    return FilterAdapter(
        mutableMapOf(
            FilterCategory.Platform to emptyList(),
            FilterCategory.ContentType to emptyList(),
            FilterCategory.Difficulty to emptyList(),
            FilterCategory.Category to emptyList())
    ) { filterHeader ->
      filterHeader?.let {
        when (it) {
          FilterCategory.Platform -> {
            loadPlatforms()
          }
          FilterCategory.Category -> {
            loadCategories()
          }
          FilterCategory.ContentType -> {
            val contentTypeList =
                viewModel.getContentTypeList(resources.getStringArray(R.array.filter_content_type))
            filterAdapter.setFilterOptions(contentTypeList,
                FilterCategory.ContentType)
          }
          FilterCategory.Difficulty -> {
            val difficultyList =
                viewModel.getDifficultyList(resources.getStringArray(R.array.filter_difficulty))
            filterAdapter.setFilterOptions(difficultyList,
                FilterCategory.Difficulty)
          }
        }
      }
    }
  }

  private fun loadPlatforms() {
    viewModel.domains.observe(this, Observer { domainList ->
      filterAdapter.setFilterOptions(domainList,
          FilterCategory.Platform)
    })

    viewModel.getDomains()
  }

  private fun loadCategories() {
    viewModel.categories.observe(this, Observer { categoryList ->
      filterAdapter.setFilterOptions(categoryList,
          FilterCategory.Category)
    })

    viewModel.getCategories()
  }

  private fun loadFilters() {
    filterAdapter.setSelectedOptions(parentViewModel.getSelectedFilters())
  }
}
