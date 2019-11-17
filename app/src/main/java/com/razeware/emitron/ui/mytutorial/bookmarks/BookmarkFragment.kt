package com.razeware.emitron.ui.mytutorial.bookmarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.razeware.emitron.R
import com.razeware.emitron.databinding.FragmentBookmarksBinding
import com.razeware.emitron.di.modules.viewmodel.ViewModelFactory
import com.razeware.emitron.model.Data
import com.razeware.emitron.ui.common.PagedAdapter
import com.razeware.emitron.ui.common.ShimmerProgressDelegate
import com.razeware.emitron.ui.common.StartEndBottomMarginDecoration
import com.razeware.emitron.ui.common.SwipeActionCallback
import com.razeware.emitron.ui.content.ContentAdapter
import com.razeware.emitron.ui.content.ContentPagedFragment
import com.razeware.emitron.ui.mytutorial.MyTutorialFragmentDirections
import com.razeware.emitron.utils.NetworkState
import com.razeware.emitron.utils.extensions.*
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * Bookmark view
 */
class BookmarkFragment : DaggerFragment() {

  /**
   * Custom factory for viewmodel
   *
   * Custom factory provides app related dependencies
   */
  @Inject
  lateinit var viewModelFactory: ViewModelFactory

  private val viewModel: BookmarkViewModel by viewModels { viewModelFactory }

  internal val adapter by lazy {
    ContentAdapter.build(
      pagedAdapter = PagedAdapter(),
      onItemClick = ::openCollection,
      retryCallback = ::loadBookmarks,
      emptyCallback = ::handleEmpty,
      onItemRetry = ::loadBookmarks,
      adapterContentType = ContentAdapter.AdapterContentType.ContentBookmarked,
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

  private lateinit var progressDelegate: ShimmerProgressDelegate

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
    pagedFragment.value.initPaging(this, binding.recyclerView)
    binding.recyclerView.addItemDecoration(StartEndBottomMarginDecoration())
    progressDelegate = ShimmerProgressDelegate(requireView())
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
    viewModel.getPaginationViewModel().networkState.observe(viewLifecycleOwner) {
      handleInitialProgress(it)
    }

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

  private fun handleInitialProgress(networkState: NetworkState?) {
    when (networkState) {
      NetworkState.INIT -> {
        progressDelegate.showProgressView()
      }
      NetworkState.SUCCESS,
      NetworkState.INIT_SUCCESS -> {
        addSwipeToDelete()
        progressDelegate.hideProgressView()
      }
      NetworkState.INIT_EMPTY,
      NetworkState.INIT_FAILED,
      NetworkState.FAILED -> {
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
