package com.razeware.emitron.ui.download

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.work.WorkManager
import com.google.android.exoplayer2.offline.DownloadManager
import com.razeware.emitron.R
import com.razeware.emitron.databinding.FragmentDownloadsBinding
import com.razeware.emitron.model.Data
import com.razeware.emitron.ui.common.PagedAdapter
import com.razeware.emitron.ui.common.StartEndBottomMarginDecoration
import com.razeware.emitron.ui.common.SwipeActionCallback
import com.razeware.emitron.ui.content.ContentAdapter
import com.razeware.emitron.ui.content.ContentPagedFragment
import com.razeware.emitron.ui.download.helpers.DownloadHelper
import com.razeware.emitron.ui.download.helpers.DownloadProgressHelper
import com.razeware.emitron.ui.download.workers.RemoveDownloadWorker
import com.razeware.emitron.ui.mytutorial.MyTutorialFragmentDirections
import com.razeware.emitron.ui.onboarding.OnboardingView
import com.razeware.emitron.utils.UiStateManager
import com.razeware.emitron.utils.extensions.launchCustomTab
import com.razeware.emitron.utils.extensions.setDataBindingView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Downloads view
 */
@AndroidEntryPoint
class DownloadFragment : Fragment() {

  /**
   * Download manager instance to observe download progress
   */
  @Inject
  lateinit var downloadManager: DownloadManager

  private val viewModel: DownloadViewModel by viewModels()

  private val swipeActionCallback by lazy {
    SwipeActionCallback.build(
      R.drawable.bg_swipe_bookmark,
      R.string.button_delete,
      onSwipe = {
        deleteDownload(adapter.getItemFor(it))
      })
  }

  private val itemTouchHelper = ItemTouchHelper(swipeActionCallback)

  internal val adapter by lazy {
    ContentAdapter.build(
      pagedAdapter = PagedAdapter(),
      onItemClick = ::openCollection,
      retryCallback = ::loadDownloads,
      emptyCallback = ::handleEmpty,
      onItemRetry = ::loadDownloads,
      type = ContentAdapter.Type.ContentDownloaded,
      downloadCallback = { data, _ -> handleDownload(data) }
    )
  }

  private val pagedFragment = lazy {
    ContentPagedFragment(
      viewModel.getPaginationViewModel(),
      adapter
    )
  }

  private val downloadHelper: DownloadHelper by lazy {
    DownloadHelper(this)
  }

  private lateinit var binding: FragmentDownloadsBinding

  /**
   * Helper to observer download progress from [DownloadManager]
   */
  @Inject
  lateinit var downloadProgressHelper: DownloadProgressHelper

  /**
   * See [Fragment.onCreateView]
   */
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = setDataBindingView(R.layout.fragment_downloads, container)
    return binding.root
  }

  /**
   * See [Fragment.onViewCreated]
   */
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initUi()
    loadDownloads()
    checkAndShowOnboarding()
    checkAndShowDownloadSubscription()
    initDownloadProgress()
  }

  private fun initDownloadProgress() {
    downloadProgressHelper.init(isVisible, requireContext(), emptyList()) {
      viewModel.updateDownload(it)
    }
  }

  private fun initUi() {
    pagedFragment.value.initPaging(
      this,
      binding.recyclerView,
      onUiStateChange = ::handleInitialProgress
    )
    binding.recyclerView.addItemDecoration(StartEndBottomMarginDecoration())
    binding.buttonManageSubscription.setOnClickListener {
      launchCustomTab(Uri.parse(getString(R.string.manage_subscription_url)))
    }
  }

  private fun addSwipeToDelete() {
    itemTouchHelper.attachToRecyclerView(binding.recyclerView)
  }

  private fun removeSwipeToDelete() {
    itemTouchHelper.attachToRecyclerView(null)
  }

  private fun deleteDownload(data: Data?) {
    val contentId = data?.id
    contentId?.let {
      RemoveDownloadWorker.enqueue(
        WorkManager.getInstance(requireContext()),
        contentId
      )
    }
  }

  private fun handleInitialProgress(uiState: UiStateManager.UiState?) {
    when (uiState) {
      UiStateManager.UiState.INIT -> {
      }
      UiStateManager.UiState.LOADED,
      UiStateManager.UiState.INIT_LOADED -> {
        addSwipeToDelete()
      }
      UiStateManager.UiState.INIT_EMPTY,
      UiStateManager.UiState.INIT_FAILED,
      UiStateManager.UiState.ERROR -> {
        removeSwipeToDelete()
      }
      else -> {
        // NA
      }
    }
  }

  private fun loadDownloads() {
    viewModel.loadDownloads()
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
      .navigate(R.id.action_navigation_downloads_to_navigation_library)
  }

  private fun checkAndShowOnboarding() {
    val canShowDownloadOnboarding =
      viewModel.isOnboardingAllowed() &&
          !viewModel.isOnboardedForType(OnboardingView.Download)

    if (canShowDownloadOnboarding) {
      val action = DownloadFragmentDirections.actionNavigationDownloadsToNavigationOnboarding(
        OnboardingView.Download
      )
      findNavController().navigate(action)
    }
  }

  private fun checkAndShowDownloadSubscription() {
    binding.groupDownloadNoSubscription.isVisible =
      !viewModel.isDownloadAllowed()
  }

  private fun handleDownload(content: Data? = null) {
    val contentId = content?.id ?: return
    downloadHelper.showDeleteDownloadedContentDialog(contentId)
  }
}
