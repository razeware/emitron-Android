package com.raywenderlich.emitron.ui.mytutorial.progressions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.databinding.FragmentBookmarksBinding
import com.raywenderlich.emitron.di.modules.viewmodel.ViewModelFactory
import com.raywenderlich.emitron.model.CompletionStatus
import com.raywenderlich.emitron.model.Data
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
 * In-Progress/Completed view
 */
class ProgressionFragment : DaggerFragment() {

  companion object {

    private const val EXTRA_COMPLETION_STATUS = "completion_status"

    /**
     * Create a new instance of ProgressionFragment
     * with completion status [CompletionStatus.InProgress]
     */
    fun newInstanceInProgress(): ProgressionFragment {
      val fragment = ProgressionFragment()
      fragment.arguments = Bundle().apply {
        putParcelable(EXTRA_COMPLETION_STATUS, CompletionStatus.InProgress)
      }
      return fragment
    }
  }

  /**
   * Custom factory for viewmodel
   *
   * Custom factory provides app related dependencies
   */
  @Inject
  lateinit var viewModelFactory: ViewModelFactory

  private val viewModel: ProgressionViewModel by viewModels { viewModelFactory }

  private var completionStatus = CompletionStatus.Completed

  internal val adapter: ContentAdapter by lazy {
    ContentAdapter.build(
      adapterContentType = getAdapterContentType(),
      onItemClick = ::openCollection,
      onItemRetry = ::loadProgressions,
      retryCallback = ::loadProgressions,
      emptyCallback = ::handleEmpty
    )
  }

  private val pagedFragment = lazy {
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
    binding = setDataBindingView(inflater, R.layout.fragment_bookmarks, container)
    return binding.root
  }

  /**
   * See [androidx.fragment.app.Fragment.onViewCreated]
   */
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    completionStatus =
      arguments?.getParcelable(EXTRA_COMPLETION_STATUS)
        ?: CompletionStatus.Completed
    initUi()
    initObservers()
    loadProgressions()
  }

  private fun initUi() {
    pagedFragment.value.initPaging(this, binding.recyclerView)
    binding.recyclerView.addItemDecoration(StartEndBottomMarginDecoration())
    addSwipeToUpdateProgress()
    progressDelegate = ShimmerProgressDelegate(requireView())
  }

  private fun addSwipeToUpdateProgress() {
    val swipeHandler = object : SwipeActionCallback(
      R.drawable.bg_swipe_progression,
      getSwipeText()
    ) {
      override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        updateContentProgression(adapter.getItemFor(viewHolder))
      }
    }
    val itemTouchHelper = ItemTouchHelper(swipeHandler)
    itemTouchHelper.attachToRecyclerView(binding.recyclerView)
  }

  private fun getSwipeText(): Int = if (completionStatus == CompletionStatus.Completed) {
    R.string.button_mark_in_progress
  } else {
    R.string.button_mark_completed
  }

  private fun getAdapterContentType(): ContentAdapter.AdapterContentType =
    if (completionStatus == CompletionStatus.Completed) {
      ContentAdapter.AdapterContentType.ContentCompleted
    } else {
      ContentAdapter.AdapterContentType.ContentInProgress
    }

  internal fun updateContentProgression(data: Data?) {
    viewModel.updateContentProgression(data)
  }

  private fun initObservers() {
    viewModel.getPaginationViewModel().networkState.observe(viewLifecycleOwner) {
      handleInitialProgress(it)
    }

    viewModel.completionActionResult.observe(viewLifecycleOwner) {
      val (event, _) =
        it ?: (null to 0)
      when (event?.getContentIfNotHandled()) {
        ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeMarkedCompleted -> {
          showSuccessSnackbar(getString(R.string.message_episode_marked_completed))
        }
        ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeMarkedInProgress ->
          showSuccessSnackbar(getString(R.string.message_episode_marked_in_progress))
        ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeFailedToMarkComplete -> {
          showErrorSnackbar(getString(R.string.message_episode_failed_to_mark_completed))
        }
        ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeFailedToMarkInProgress -> {
          showErrorSnackbar(
            getString(
              R.string.message_episode_failed_to_mark_in_progress
            )
          )
        }
        null -> {
          // Houston, We Have a Problem!
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
      NetworkState.SUCCESS -> {
        progressDelegate.hideProgressView()
      }
      else -> {
        // Ignore
      }
    }
  }

  private fun loadProgressions() {
    if (isNetNotConnected()) {
      pagedFragment.value.onErrorConnection()
      progressDelegate.hideProgressView()
      return
    }

    viewModel.loadProgressions(completionStatus)
  }

  private fun openCollection(collection: Data?) {
    collection?.let {
      val action = MyTutorialFragmentDirections
        .actionNavigationMyTutorialsNavigationCollection(collection = collection)
      findNavController().navigate(action)
    }
  }

  private fun handleEmpty() {
    findNavController()
      .navigate(R.id.action_navigation_my_tutorials_to_navigation_library)
  }
}
