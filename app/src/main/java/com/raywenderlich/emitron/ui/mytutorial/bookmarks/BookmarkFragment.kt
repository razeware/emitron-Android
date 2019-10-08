package com.raywenderlich.emitron.ui.mytutorial.bookmarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.databinding.FragmentBookmarksBinding
import com.raywenderlich.emitron.di.modules.viewmodel.ViewModelFactory
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.ui.common.PagedAdapter
import com.raywenderlich.emitron.ui.common.ShimmerProgressDelegate
import com.raywenderlich.emitron.ui.content.ContentAdapter
import com.raywenderlich.emitron.ui.content.ContentPagedFragment
import com.raywenderlich.emitron.ui.mytutorial.MyTutorialFragmentDirections
import com.raywenderlich.emitron.utils.NetworkState
import com.raywenderlich.emitron.utils.StartEndBottomMarginDecoration
import com.raywenderlich.emitron.utils.SwipeActionCallback
import com.raywenderlich.emitron.utils.extensions.*
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
    addSwipeToDelete()
    progressDelegate = ShimmerProgressDelegate(requireView())
  }

  private fun addSwipeToDelete() {
    val swipeHandler = SwipeActionCallback.build(
      R.drawable.bg_swipe_bookmark,
      R.string.button_delete,
      onSwipe = {
        updateContentBookmark(adapter.getItemFor(it))
      }
    )
    val itemTouchHelper = ItemTouchHelper(swipeHandler)
    itemTouchHelper.attachToRecyclerView(binding.recyclerView)
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
      NetworkState.INIT_SUCCESS,
      NetworkState.INIT_EMPTY,
      NetworkState.INIT_FAILED,
      NetworkState.FAILED,
      NetworkState.SUCCESS -> {
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
    findNavController()
      .navigate(R.id.action_navigation_my_tutorials_to_navigation_library)
  }
}
