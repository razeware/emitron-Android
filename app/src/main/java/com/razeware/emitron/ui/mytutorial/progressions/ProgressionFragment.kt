package com.razeware.emitron.ui.mytutorial.progressions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.work.WorkManager
import com.razeware.emitron.R
import com.razeware.emitron.databinding.FragmentBookmarksBinding
import com.razeware.emitron.model.CompletionStatus
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.isCompleted
import com.razeware.emitron.ui.common.ProgressDelegate
import com.razeware.emitron.ui.common.StartEndBottomMarginDecoration
import com.razeware.emitron.ui.common.SwipeActionCallback
import com.razeware.emitron.ui.content.ContentAdapter
import com.razeware.emitron.ui.content.ContentPagedFragment
import com.razeware.emitron.ui.mytutorial.MyTutorialFragmentDirections
import com.razeware.emitron.ui.player.workers.UpdateOfflineProgressWorker
import com.razeware.emitron.utils.UiStateManager
import com.razeware.emitron.utils.extensions.*
import dagger.hilt.android.AndroidEntryPoint

/**
 * In-Progress/Completed view
 */
@AndroidEntryPoint
class ProgressionFragment : Fragment() {

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

  private val viewModel: ProgressionViewModel by viewModels()

  private var completionStatus = CompletionStatus.Completed

  internal val adapter: ContentAdapter by lazy {
    ContentAdapter.build(
      type = getAdapterContentType(),
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

  private lateinit var progressDelegate: ProgressDelegate

  private val swipeActionCallback by lazy {
    SwipeActionCallback.build(
      getSwipeBackground(),
      getSwipeText(), onSwipe = {
        updateContentProgression(adapter.getItemFor(it))
      }
    )
  }

  private val itemTouchHelper by lazy { ItemTouchHelper(swipeActionCallback) }

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
    completionStatus =
      arguments?.getParcelable(EXTRA_COMPLETION_STATUS) ?: CompletionStatus.Completed
    initUi()
    initObservers()
    loadProgressions()
  }

  private fun initUi() {
    pagedFragment.value.initPaging(
      this, binding.recyclerView,
      onUiStateChange = ::handleInitialProgress
    )
    binding.recyclerView.addItemDecoration(StartEndBottomMarginDecoration())
    progressDelegate = ProgressDelegate(requireView())
  }

  private fun addSwipeToUpdateProgress() {
    itemTouchHelper.attachToRecyclerView(binding.recyclerView)
  }

  private fun removeSwipeToUpdateProgress() {
    itemTouchHelper.attachToRecyclerView(null)
  }


  private fun getSwipeText(): Int =
    if (completionStatus.isCompleted()) {
      R.string.button_mark_in_progress
    } else {
      R.string.button_mark_completed
    }

  private fun getSwipeBackground(): Int =
    if (completionStatus.isCompleted()) {
      R.drawable.bg_swipe_progression_delete
    } else {
      R.drawable.bg_swipe_progression
    }

  private fun getAdapterContentType(): ContentAdapter.Type =
    if (completionStatus.isCompleted()) {
      ContentAdapter.Type.ContentCompleted
    } else {
      ContentAdapter.Type.ContentInProgress
    }

  private fun updateContentProgression(data: Data?) {
    viewModel.updateContentProgression(isNetConnected(), data)
  }

  private fun initObservers() {
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

    viewModel.enqueueOfflineProgressUpdate.observe(viewLifecycleOwner) {
      it?.let {
        UpdateOfflineProgressWorker.enqueue(WorkManager.getInstance(requireContext()))
      }
    }
  }

  private fun handleInitialProgress(uiState: UiStateManager.UiState?) {
    when (uiState) {
      UiStateManager.UiState.INIT -> {
        progressDelegate.showProgressView()
      }
      UiStateManager.UiState.INIT_LOADED -> {
        addSwipeToUpdateProgress()
        progressDelegate.hideProgressView()
      }
      UiStateManager.UiState.INIT_EMPTY,
      UiStateManager.UiState.INIT_FAILED,
      UiStateManager.UiState.ERROR -> {
        removeSwipeToUpdateProgress()
        progressDelegate.hideProgressView()
      }
      UiStateManager.UiState.LOADED -> {
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
      val navController = findNavController()
      if (navController.currentDestination?.id == R.id.navigation_my_tutorials) {
        val action = MyTutorialFragmentDirections
          .actionNavigationMyTutorialsToNavigationCollection(collection = collection)
        findNavController().navigate(action)
      }
    }
  }

  private fun handleEmpty() {
    findNavController()
      .navigate(R.id.action_navigation_my_tutorials_to_navigation_library)
  }
}
