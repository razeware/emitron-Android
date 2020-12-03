package com.razeware.emitron.ui.filter

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.core.view.ViewCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.razeware.emitron.MainViewModel
import com.razeware.emitron.R
import com.razeware.emitron.databinding.FragmentFilterBinding
import com.razeware.emitron.model.ContentType
import com.razeware.emitron.ui.common.getDefaultAppBarConfiguration
import com.razeware.emitron.utils.extensions.observe
import com.razeware.emitron.utils.extensions.setDataBindingView
import com.razeware.emitron.utils.extensions.showErrorSnackbar
import dagger.hilt.android.AndroidEntryPoint

/**
 * Filter view
 */
@AndroidEntryPoint
class FilterFragment : Fragment() {

  private val viewModel: FilterViewModel by viewModels()

  private val parentViewModel: MainViewModel by activityViewModels()

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
    binding = setDataBindingView(R.layout.fragment_filter, container)
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

  /**
   * Any time the screen loads, we check if the device supports cutouts and try to adjust our
   * padding accordingly.
   * */
  override fun onResume() {
    super.onResume()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      setupWindowInsets()
    }
  }

  /**
   * Similarly to what we do on the [MainActivity], we add insets to this screen if there's a bottom
   * navigation bar.
   * */
  @TargetApi(Build.VERSION_CODES.P)
  private fun setupWindowInsets() {
    binding.filterRoot.doOnLayout {
      val inset = binding.filterRoot.rootWindowInsets

      val cutoutSize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        inset?.getInsets(WindowInsets.Type.navigationBars())?.bottom
      } else {
        inset?.displayCutout?.safeInsetBottom
      }

      if (cutoutSize != null) {
        binding.bottomPadding = cutoutSize
      }
    }
  }

  private fun initToolbar() {
    binding.toolbar.setupWithNavController(
      findNavController(),
      getDefaultAppBarConfiguration()
    )
    binding.toolbar.navigationIcon =
      VectorDrawableCompat.create(resources, R.drawable.ic_material_icon_close, null)
  }

  private fun initUi() {
    binding.buttonFilterClose.setOnClickListener {
      parentViewModel.setSelectedFilters(filterAdapter.getSelectedOptions())
      findNavController().navigate(R.id.action_navigation_filter_to_navigation_library)
    }
    binding.buttonFilterClear.setOnClickListener {
      parentViewModel.setSelectedFilters(emptyList())
      findNavController().navigate(R.id.action_navigation_filter_to_navigation_library)
    }

    with(binding.recyclerViewFilter) {
      layoutManager = LinearLayoutManager(context)
      setHasFixedSize(true)
      adapter = filterAdapter
    }
    viewModel.loadFilterOptionsResult.observe(viewLifecycleOwner) {

      when (it?.getContentIfNotHandled()) {
        FilterViewModel.LoadFilterOptionResult.FailedToFetchDomains -> {
          binding.filterProgress.visibility = View.GONE
          if (!viewModel.hasDomains()) {
            showErrorSnackbar(getString(R.string.error_load_failed_domains))
          }
        }
        FilterViewModel.LoadFilterOptionResult.FailedToFetchCategories -> {
          binding.filterProgress.visibility = View.GONE
          if (!viewModel.hasCategories()) {
            showErrorSnackbar(getString(R.string.error_load_failed_categories))
          }
        }
        FilterViewModel.LoadFilterOptionResult.FetchingFilterOption ->
          binding.filterProgress.visibility = View.VISIBLE
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
        FilterCategory.Category to emptyList()
      )
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
            loadContentTypes()
          }
          FilterCategory.Difficulty -> {
            loadDifficulty()
          }
        }
      }
    }
  }

  private fun loadPlatforms() {
    viewModel.domains.observe(this, Observer { domainList ->
      binding.filterProgress.visibility = View.GONE
      filterAdapter.setFilterOptions(
        domainList,
        FilterCategory.Platform
      )
    })

    viewModel.getDomains()
  }

  private fun loadCategories() {
    viewModel.categories.observe(this, Observer { categoryList ->
      binding.filterProgress.visibility = View.GONE
      filterAdapter.setFilterOptions(
        categoryList,
        FilterCategory.Category
      )
    })

    viewModel.getCategories()
  }

  private fun loadContentTypes() {
    val contentTypes =
      ContentType.getFilterContentTypes()
        .associateBy({ contentType: ContentType -> contentType }, { contentType ->
          when (contentType) {
            ContentType.Collection -> getString(R.string.content_type_video_course)
            ContentType.Screencast -> getString(R.string.content_type_screencast)
            ContentType.Episode -> getString(R.string.content_type_episode)
            ContentType.Professional -> getString(R.string.content_type_video_course_pro)
          }
        })
    val contentTypeList =
      viewModel.getContentTypeList(contentTypes)
    filterAdapter.setFilterOptions(
      contentTypeList,
      FilterCategory.ContentType
    )
  }

  private fun loadDifficulty() {
    val difficultyList =
      viewModel.getDifficultyList(resources.getStringArray(R.array.filter_difficulty))
    filterAdapter.setFilterOptions(
      difficultyList,
      FilterCategory.Difficulty
    )
  }

  private fun loadFilters() {
    filterAdapter.setSelectedOptions(parentViewModel.getSelectedFilters())

    if (parentViewModel.getSelectedFilters().isNotEmpty()) {
      loadPlatforms()
      loadCategories()
      loadContentTypes()
      loadDifficulty()
    }
  }
}
