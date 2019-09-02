package com.raywenderlich.emitron.ui.library

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
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
  }, pagedAdapter = PagedAdapter())

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

  @SuppressLint("ClickableViewAccessibility")
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
      showSortPopup()
    }

    progressDelegate = ShimmerProgressDelegate(requireView())

    binding.editTextLibrarySearch.setOnTouchListener { _, _ ->
      toggleRecentSearchView()
      false
    }

    binding.editTextLibrarySearch.setOnEditorActionListener { _, actionId, _ ->
      if (actionId == EditorInfo.IME_ACTION_SEARCH) {
        handleSearchImeAction()
        true
      } else {
        false
      }
    }

    binding.textInputLayoutSearch.setStartIconOnClickListener {
      showRecentSearchControls()
    }

    binding.textInputLayoutSearch.setEndIconOnClickListener {
      handleQueryCleared()
    }
  }

  private fun showSortPopup() {
    val popup = PopupMenu(
      requireContext(),
      binding.buttonLibrarySort,
      Gravity.END,
      0,
      R.style.AppTheme_Popup
    )
    popup.menuInflater.inflate(R.menu.menu_library_sort, popup.menu)
    popup.setOnMenuItemClickListener {
      parentViewModel.setSortOrder(it.title.toString())
      loadCollections()
      true
    }
    popup.show()
  }

  private fun showRecentSearchControls() {
    val isRecentSearchViewShowing = binding.recyclerViewLibraryRecent.isVisible
    if (isRecentSearchViewShowing) {
      toggleControls(visible = true)
      hideRecentSearchControls()
    } else {
      if (toggleRecentSearchView()) {
        binding.textInputLayoutSearch.setStartIconDrawable(R.drawable.ic_material_icon_arrow_back)
      }
    }
  }

  private fun hideRecentSearchControls() {
    with(binding) {
      textInputLayoutSearch.setStartIconDrawable(R.drawable.ic_material_icon_search)
      recyclerViewLibraryRecent.visibility = View.GONE
      hideKeyboard()
    }
  }

  private fun toggleRecentSearchView(): Boolean {
    val recyclerView = binding.recyclerViewLibraryRecent

    // Get recent search terms
    toggleControls(visible = false)
    val recentSearchTerms = viewModel.loadSearchQueries()
    if (recentSearchTerms.isNotEmpty()) {
      with(recyclerView) {
        layoutManager = object : LinearLayoutManager(requireContext()) {
          override fun canScrollVertically(): Boolean = false
        }
        adapter = RecentSearchAdapter(recentSearchTerms) {
          handleRecentSearchItemSelected(it)
        }
      }
      recyclerView.visibility = View.VISIBLE
    }

    return recyclerView.isVisible
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
      if (it.isNullOrBlank()) {
        if (!parentViewModel.hasFilters()) {
          adapter.updateContentType()
        }
      } else {
        if (!parentViewModel.hasFilters()) {
          adapter.updateContentType(ContentAdapter.ContentAdapterType.ContentWithSearch)
        }
      }
    }
    parentViewModel.sortOrder.observe(viewLifecycleOwner) {
      binding.buttonLibrarySort.text = it ?: getString(R.string.button_newest)
    }
  }

  private fun handleInitialProgress(networkState: NetworkState?) {
    when (networkState) {
      NetworkState.INIT -> {
        progressDelegate.showProgressView()
        toggleControls()

      }
      NetworkState.INIT_SUCCESS, NetworkState.INIT_EMPTY -> {
        toggleControls(true)
        hideRecentSearchControls()
        progressDelegate.hideProgressView()
      }
      else -> {
        // Handled by the adapter
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

    viewModel.loadCollections(
      parentViewModel.getSelectedFilters(
        withSearch = true,
        withSort = true
      )
    )
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

    val selectedFilters = parentViewModel.getSelectedFilters()
    clearAllFilters()
    if (selectedFilters.size > 1) {
      addClearAllChip()
    }
    selectedFilters.map {
      addFilterChip(it)
    }
  }

  private fun clearAllFilters() {
    val filterContainer = binding.chipGroupLibraryFilter
    filterContainer.removeAllViews()
    filterContainer.visibility = View.GONE
    adapter.updateContentType(ContentAdapter.ContentAdapterType.Content)
  }

  private fun applyDefaultChipStyle(chip: Chip) {
    val shapeAppearanceModel =
      ShapeAppearanceModel().apply {
        setCornerRadius(9.0f.toInt().toPx().toFloat())
      }
    with(chip) {
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

  private fun addClearAllChip() {
    val filterContainer = binding.chipGroupLibraryFilter
    filterContainer.visibility = View.VISIBLE
    if (filterContainer.childCount <= 0) {
      val closeChip = Chip(requireContext()).apply {
        text = getString(R.string.button_filter_clear_all)
        applyDefaultChipStyle(this)
        setChipBackgroundColorResource(R.color.colorError)
        setCloseIconTintResource(R.color.colorIconOnError)
        setTextAppearance(R.style.TextAppearance_Button_Small_Inverse)
      }
      closeChip.setOnCloseIconClickListener {
        filterContainer.removeAllViews()
        filterContainer.visibility = View.GONE
        parentViewModel.resetFilters()
        adapter.updateContentType()
        loadCollections()
      }
      filterContainer.addView(closeChip as View)
    }
  }

  private fun addFilterChip(filter: Data) {
    val filterContainer = binding.chipGroupLibraryFilter
    with(filter) {
      val chip = Chip(requireContext()).apply {
        text = getName()
        tag = filter
        applyDefaultChipStyle(this)
      }
      chip.setOnCloseIconClickListener {
        parentViewModel.removeFilter(it.tag as? Data)
        if (parentViewModel.getSelectedFilters().isEmpty()) {
          clearAllFilters()
        }
        loadCollections()
      }
      filterContainer.addView(chip as View)
      adapter.updateContentType(ContentAdapter.ContentAdapterType.ContentWithFilters)
      filterContainer.visibility = View.VISIBLE
    }
  }

  private fun handleSearchImeAction() {
    val query = binding.editTextLibrarySearch.text?.toString()
    if (!query.isNullOrBlank()) {
      viewModel.saveSearchQuery(query)
      parentViewModel.setSearchQuery(query)
      hideRecentSearchControls()
      loadCollections()
    }
  }

  private fun handleQueryCleared() {
    val lastQuery = parentViewModel.query.value
    val query = binding.editTextLibrarySearch.text?.toString()
    if (!query.isNullOrBlank()) {

      if (binding.recyclerViewLibrary.isVisible && parentViewModel.clearSearchQuery()) {
        hideRecentSearchControls()
        loadCollections()
        return
      }

      if (!lastQuery.isNullOrBlank() && lastQuery == query) {
        if (parentViewModel.clearSearchQuery()) {
          loadCollections()
          return
        }
      }
    }
  }

  private fun handleRecentSearchItemSelected(query: String) {
    viewModel.saveSearchQuery(query)
    parentViewModel.setSearchQuery(query)
    binding.editTextLibrarySearch.setText(query)
    hideRecentSearchControls()
    loadCollections()
  }
}
