package com.razeware.emitron.ui.library

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo.*
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.shape.ShapeAppearanceModel
import com.razeware.emitron.MainViewModel
import com.razeware.emitron.R
import com.razeware.emitron.databinding.FragmentLibraryBinding
import com.razeware.emitron.model.Data
import com.razeware.emitron.ui.common.BottomMarginDecoration
import com.razeware.emitron.ui.common.ProgressDelegate
import com.razeware.emitron.ui.content.ContentAdapter
import com.razeware.emitron.ui.content.ContentPagedFragment
import com.razeware.emitron.ui.library.search.RecentSearchAdapter
import com.razeware.emitron.utils.UiStateManager
import com.razeware.emitron.utils.extensions.*
import dagger.hilt.android.AndroidEntryPoint


/**
 * Library view
 */
@AndroidEntryPoint
class LibraryFragment : Fragment() {

  private val viewModel: LibraryViewModel by viewModels()

  private val parentViewModel: MainViewModel by activityViewModels()

  private lateinit var binding: FragmentLibraryBinding

  private lateinit var progressDelegate: ProgressDelegate

  private val adapter by lazy {
    ContentAdapter.build(
      onItemClick = ::openCollection,
      onItemRetry = ::handleItemRetry,
      retryCallback = ::loadCollections
    )
  }

  private val pagedFragment = lazy(LazyThreadSafetyMode.NONE) {
    ContentPagedFragment(
      viewModel.getPaginationViewModel(),
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
      R.layout.fragment_library, container
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
    pagedFragment.value.initPaging(
      this, binding.recyclerViewLibrary,
      onUiStateChange = ::handleInitialProgress
    ) {
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
      parentViewModel.setSortOrder(binding.buttonLibrarySort.text.toString())
      loadCollections()
    }

    progressDelegate = ProgressDelegate(requireView())

    binding.editTextLibrarySearch.setOnTouchListener { _, _ ->
      toggleRecentSearchView()
      false
    }

    binding.editTextLibrarySearch.setOnEditorActionListener { _, actionId, _ ->
      val allowedImeActions = arrayOf(
        IME_ACTION_SEARCH, IME_ACTION_DONE, IME_ACTION_GO,
        IME_ACTION_UNSPECIFIED
      )
      if (actionId in allowedImeActions) {
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

    binding.libraryPullToRefresh.setOnRefreshListener {
      loadCollections()
    }
  }

  private fun showRecentSearchControls() {
    val isRecentSearchViewShowing = binding.recyclerViewLibraryRecent.isVisible
    if (isRecentSearchViewShowing) {
      toggleControls(visible = true)
      hideRecentSearchControls()
    } else {
      toggleRecentSearchView()
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
      toggleControls(visible = false)
      binding.textInputLayoutSearch.setStartIconDrawable(R.drawable.ic_material_icon_arrow_back)
    }

    return recyclerView.isVisible
  }

  private fun initObservers() {
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
          adapter.updateContentType(ContentAdapter.Type.ContentWithSearch)
        }
      }
    }
    parentViewModel.sortOrder.observe(viewLifecycleOwner) {
      binding.buttonLibrarySort.text = it?.capitalize() ?: getString(R.string.button_newest)
    }
  }

  private fun handleInitialProgress(uiState: UiStateManager.UiState?) {
    when (uiState) {
      UiStateManager.UiState.INIT -> {
        progressDelegate.showProgressView()
        toggleControls()
      }
      UiStateManager.UiState.INIT_LOADED, UiStateManager.UiState.INIT_EMPTY -> {
        toggleControls(true, uiState == UiStateManager.UiState.INIT_EMPTY)
        hideRecentSearchControls()
        progressDelegate.hideProgressView()
        binding.libraryPullToRefresh.isRefreshing = false
      }
      UiStateManager.UiState.INIT_FAILED -> {
        loadCollections() // retry
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

    viewModel.syncDomainsAndCategories()
  }

  private fun openCollection(collection: Data?) {
    collection?.let {
      val action = LibraryFragmentDirections
        .actionNavigationLibraryToNavigationCollection(collection = collection)
      findNavController().navigate(action)
    }
  }

  private fun toggleControls(visible: Boolean = false, isEmpty: Boolean = false) {
    val visibility = if (visible) {
      View.VISIBLE
    } else {
      View.GONE
    }
    val controlVisibility = if (visible && !isEmpty) {
      View.VISIBLE
    } else {
      View.GONE
    }
    with(binding) {
      recyclerViewLibrary.visibility = visibility
      scrollViewLibraryFilter.visibility = visibility
      if (isNetConnected()) {
        buttonLibrarySort.visibility = controlVisibility
        textLibraryCount.visibility = controlVisibility
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
    adapter.updateContentType(ContentAdapter.Type.Content)
  }

  private fun applyDefaultChipStyle(chip: Chip) {
    val shapeAppearanceModel =
      ShapeAppearanceModel.builder().setAllCornerSizes(9.0f.toInt().toPx().toFloat()).build()

    with(chip) {
      this.shapeAppearanceModel = shapeAppearanceModel
      isCloseIconVisible = true
      closeIcon = context.getDrawable(R.drawable.ic_material_icon_x_light)
      setCloseIconSizeResource(R.dimen.icon_height_width_2)
      setChipIconTintResource(R.color.white)
      setChipBackgroundColorResource(R.color.colorSurface)
      isClickable = true
      isCheckable = false
      setPadding(resources.getDimensionPixelSize(R.dimen.chip_padding_default))
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
      adapter.updateContentType(ContentAdapter.Type.ContentWithFilters)
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
      } else {
        binding.editTextLibrarySearch.setText("")
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

  private fun handleItemRetry() {
    viewModel.getPaginationViewModel().handleItemRetry(isNetConnected())
  }
}
