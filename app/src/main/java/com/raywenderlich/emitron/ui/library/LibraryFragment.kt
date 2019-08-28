package com.raywenderlich.emitron.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.shape.ShapeAppearanceModel
import com.raywenderlich.emitron.MainViewModel
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.databinding.FragmentLibraryBinding
import com.raywenderlich.emitron.di.modules.viewmodel.ViewModelFactory
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.ui.common.PagedAdapter
import com.raywenderlich.emitron.ui.common.ShimmerProgressDelegate
import com.raywenderlich.emitron.ui.content.ContentAdapter
import com.raywenderlich.emitron.ui.content.ContentPagedFragment
import com.raywenderlich.emitron.ui.library.search.RecentSearchAdapter
import com.raywenderlich.emitron.utils.BottomMarginDecoration
import com.raywenderlich.emitron.utils.NetworkState
import com.raywenderlich.emitron.utils.extensions.*
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

  private lateinit var progressDelegate: ShimmerProgressDelegate

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
        Navigation.createNavigateOnClickListener(
            R.id.action_navigation_library_to_navigation_filter
        )
    )

    binding.buttonLibrarySort.setOnClickListener {

    }
    progressDelegate = ShimmerProgressDelegate(requireView())

    binding.editTextLibrarySearch.setOnFocusChangeListener { _, hasFocus ->
      if (hasFocus) {
        initRecentSearchRecyclerView()
      }
    }

    binding.editTextLibrarySearch.setOnEditorActionListener { _, actionId, _ ->
      if (actionId == EditorInfo.IME_ACTION_SEARCH) {
        val searchTerm = binding.editTextLibrarySearch.text?.toString()
        if (!searchTerm.isNullOrBlank()) {
          viewModel.saveSearchQuery(searchTerm)
          parentViewModel.setSearchQuery(searchTerm)
          hideRecentSearchControls()
          loadCollections()
        }
        true
      } else {
        false
      }
    }

    binding.textInputLayoutSearch.setEndIconOnClickListener {
      val searchTerm = binding.editTextLibrarySearch.text?.toString()

      if (!searchTerm.isNullOrBlank()) {
        if (parentViewModel.clearSearchQuery()) {
          hideRecentSearchControls()
          loadCollections()
        }
        binding.editTextLibrarySearch.setText("")
      }
    }
  }

  private fun hideRecentSearchControls() {
    with(binding) {
      textInputLayoutSearch.setStartIconDrawable(0)
      recyclerViewLibraryRecent.visibility = View.GONE
      hideKeyboard()
    }
  }

  private fun initRecentSearchRecyclerView() {
    val recyclerView = binding.recyclerViewLibraryRecent

    // Get recent search terms
    toggleControls(visible = false)
    val recentSearchTerms = viewModel.loadSearchQueries()
    if (recentSearchTerms.isNotEmpty()) {
      with(binding.textInputLayoutSearch) {
        setStartIconDrawable(R.drawable.ic_material_icon_arrow_back)
        setStartIconOnClickListener {
          toggleControls(visible = true)
          hideRecentSearchControls()
        }
      }
      recyclerView.visibility = View.VISIBLE
    }

    with(recyclerView) {
      layoutManager = object : LinearLayoutManager(requireContext()) {
        override fun canScrollVertically(): Boolean = false
      }
      adapter = RecentSearchAdapter(recentSearchTerms) {
        viewModel.saveSearchQuery(it)
        parentViewModel.setSearchQuery(it)
        binding.editTextLibrarySearch.setText(it)
        hideRecentSearchControls()
        loadCollections()
      }
    }
  }

  private fun initObservers() {
    viewModel.contentPagedViewModel.networkState.observe(viewLifecycleOwner) {
      handleInitialProgress(it)
    }
    parentViewModel.selectedFilters.observe(viewLifecycleOwner) {
      handleFilters(it)
    }
    parentViewModel.query.observe(viewLifecycleOwner) {
      binding.editTextLibrarySearch.setText(it)
    }
  }

  private fun handleInitialProgress(networkState: NetworkState?) {
    when (networkState) {
      NetworkState.INIT -> {
        progressDelegate.showProgressView()
        toggleControls()
      }
      NetworkState.INIT_SUCCESS -> {
        progressDelegate.hideProgressView()
        toggleControls(true)
      }
      else -> {
        // Ignore
      }
    }
  }

  private fun loadCollections() {
    if (isNetNotConnected()) {
      pagedFragment.value.onErrorConnection()
      progressDelegate.hideProgressView()
      showNoInternetUI()
      return
    }

    viewModel.loadCollections(parentViewModel.getSelectedFilters())
  }

  private fun openCollection(collection: Data?) {
    collection?.let {
      val action = LibraryFragmentDirections
          .actionNavigationLibraryToNavigationCollection(collection = collection)
      findNavController().navigate(action)
    }
  }

  private fun toggleControls(visible: Boolean = false) {
    val visibility = if (visible) {
      View.VISIBLE
    } else {
      View.GONE
    }
    with(binding) {
      recyclerViewLibrary.visibility = visibility
      scrollViewLibraryFilter.visibility = visibility
      if (isNetConnected()) {
        buttonLibrarySort.visibility = visibility
        textLibraryCount.visibility = visibility
      }
    }
  }

  private fun showNoInternetUI() {
    with(binding) {
      buttonLibrarySort.visibility = View.GONE
      textLibraryCount.visibility = View.GONE
      recyclerViewLibrary.visibility = View.VISIBLE
      scrollViewLibraryFilter.visibility = View.VISIBLE
    }
  }

  private fun handleFilters(filters: List<Data>?) {
    if (filters.isNullOrEmpty()) return

    val filterWithoutSearchItems = filters.filter { !it.isTypeSearch() }

    val shapeAppearanceModel = ShapeAppearanceModel().apply {
      setCornerRadius(9.0f.toInt().toPx().toFloat())
    }
    val applyChipStyle = { chip: Chip ->
      chip.apply {
        this.shapeAppearanceModel = shapeAppearanceModel
        isCloseIconVisible = true
        closeIcon = context.getDrawable(R.drawable.ic_material_icon_close)
        setChipIconTintResource(R.color.white)
        setChipBackgroundColorResource(R.color.colorSurface)
        isClickable = true
        isCheckable = false
        setChipMinHeightResource(R.dimen.chip_height_default)
        setTextAppearance(R.style.TextAppearance_Button_Small)
      }
    }

    val filterContainer = binding.chipGroupLibraryFilter

    filterContainer.removeAllViews()
    filterContainer.visibility = View.GONE

    if (filterWithoutSearchItems.size > 1) {
      filterContainer.visibility = View.VISIBLE
      if (filterContainer.childCount <= 0) {
        val closeChip = Chip(requireContext()).apply {
          text = getString(R.string.button_filter_clear_all)
          applyChipStyle(this)
          setChipBackgroundColorResource(R.color.colorError)
          setCloseIconTintResource(R.color.colorIconOnError)
          setTextAppearance(R.style.TextAppearance_Button_Small_Inverse)
        }
        closeChip.setOnCloseIconClickListener {
          filterContainer.removeAllViews()
          filterContainer.visibility = View.GONE
          parentViewModel.resetFilters()
          adapter.hasAppliedFilters(false)
          loadCollections()
        }
        filterContainer.addView(closeChip as View)
      }
    }
    filterWithoutSearchItems.map {
      val chip = Chip(requireContext()).apply {
        text = it.getName()
        tag = it
        applyChipStyle(this)
      }
      chip.setOnCloseIconClickListener {
        parentViewModel.removeFilter(it.tag as? Data)
        loadCollections()
      }
      filterContainer.addView(chip as View)
      adapter.hasAppliedFilters()
      filterContainer.visibility = View.VISIBLE
    }
  }
}
