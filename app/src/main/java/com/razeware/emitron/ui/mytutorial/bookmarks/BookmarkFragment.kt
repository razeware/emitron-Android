package com.razeware.emitron.ui.mytutorial.bookmarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.razeware.emitron.R
import com.razeware.emitron.databinding.FragmentBookmarksBinding
import com.razeware.emitron.model.Data
import com.razeware.emitron.ui.common.PagedAdapter
import com.razeware.emitron.ui.common.ProgressDelegate
import com.razeware.emitron.ui.common.StartEndBottomMarginDecoration
import com.razeware.emitron.ui.common.SwipeActionCallback
import com.razeware.emitron.ui.content.ContentAdapter
import com.razeware.emitron.ui.content.ContentPagedFragment
import com.razeware.emitron.ui.mytutorial.MyTutorialFragmentDirections
import com.razeware.emitron.utils.UiStateManager
import com.razeware.emitron.utils.extensions.*
import dagger.hilt.android.AndroidEntryPoint

/**
 * Bookmark view
 */
@AndroidEntryPoint
class BookmarkFragment : Fragment() {

  private val viewModel: BookmarkViewModel by viewModels()

  internal val adapter by lazy {
    ContentAdapter.build(
      pagedAdapter = PagedAdapter(),
      onItemClick = ::openCollection,
      retryCallback = ::loadBookmarks,
      emptyCallback = ::handleEmpty,
      onItemRetry = ::loadBookmarks,
      type = ContentAdapter.Type.ContentBookmarked,
      bookmarkCallback = ::updateContentBookmark
    )
  }

  private val pagedFragment = lazy(LazyThreadSafetyMode.NONE) {
    ContentPagedFragment(
      viewModel.getPaginationViewModel(),
      adapter
    )
  }

  private lateinit var binding: FragmentBookmarksBinding

  private lateinit var progressDelegate: ProgressDelegate

  private val swipeActionCallback by lazy {
    SwipeActionCallback.build(
      R.drawable.bg_swipe_bookmark,
      R.string.button_delete,
      onSwipe = {
        updateContentBookmark(adapter.getItemFor(it))
      })
  }

  private val itemTouchHelper = ItemTouchHelper(swipeActionCallback)

  /**
   * See [androidx.fragment.app.Fragment.onCreateView]
   */
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = setDataBindingView(R.layout.fragment_bookmarks, container)
    return binding.root
  }

  /**
   * See [androidx.fragment.app.Fragment.onViewCreated]
   */
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initUi()
    initObservers()
    loadBookmarks()
  }

  private fun initUi() {
    pagedFragment.value.initPaging(
      this,
      binding.recyclerView,
      onUiStateChange = ::handleInitialProgress
    )
    binding.recyclerView.addItemDecoration(StartEndBottomMarginDecoration())
    progressDelegate = ProgressDelegate(requireView())
  }

  private fun addSwipeToDelete() {
    itemTouchHelper.attachToRecyclerView(binding.recyclerView)
  }

  private fun removeSwipeToDelete() {
    itemTouchHelper.attachToRecyclerView(null)
  }

  private fun updateContentBookmark(data: Data?) {
    viewModel.updateContentBookmark(data)
  }

  private fun initObservers() {
    viewModel.bookmarkDeleteActionResult.observe(viewLifecycleOwner) {
      when (it?.getContentIfNotHandled()) {
        BookmarkActionDelegate.BookmarkActionResult.BookmarkDeleted -> {
          showSuccessSnackbar(getString(R.string.message_bookmark_deleted))
        }
        BookmarkActionDelegate.BookmarkActionResult.BookmarkFailedToDelete ->
          showErrorSnackbar(getString(R.string.message_bookmark_failed_to_delete))
        else -> {
          // NA
        }
      }
    }
  }

  private fun handleInitialProgress(uiState: UiStateManager.UiState?) {
    when (uiState) {
      UiStateManager.UiState.INIT -> {
        progressDelegate.showProgressView()
      }
      UiStateManager.UiState.LOADED,
      UiStateManager.UiState.INIT_LOADED -> {
        addSwipeToDelete()
        progressDelegate.hideProgressView()
      }
      UiStateManager.UiState.INIT_EMPTY,
      UiStateManager.UiState.INIT_FAILED,
      UiStateManager.UiState.ERROR -> {
        removeSwipeToDelete()
        progressDelegate.hideProgressView()
      }
      else -> {
        // NA
      }
    }
  }

  private fun loadBookmarks() {
    if (isNetNotConnected()) {
      pagedFragment.value.onErrorConnection()
      progressDelegate.hideProgressView()
      return
    }

    viewModel.loadBookmarks()
  }

  private fun openCollection(collection: Data?) {
    collection?.let {
      val action = MyTutorialFragmentDirections
        .actionNavigationMyTutorialsToNavigationCollection(collection = collection)
      findNavController().navigate(action)
    }
  }

  private fun handleEmpty() {
    findNavController().navigate(R.id.action_navigation_my_tutorials_to_navigation_library)
  }
}
