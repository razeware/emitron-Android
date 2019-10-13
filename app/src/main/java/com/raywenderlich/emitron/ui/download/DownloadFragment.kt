package com.raywenderlich.emitron.ui.download

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.work.WorkManager
import com.google.android.exoplayer2.offline.DownloadManager
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.databinding.FragmentDownloadsBinding
import com.raywenderlich.emitron.di.modules.viewmodel.ViewModelFactory
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.ui.common.PagedAdapter
import com.raywenderlich.emitron.ui.content.ContentAdapter
import com.raywenderlich.emitron.ui.content.ContentPagedFragment
import com.raywenderlich.emitron.ui.download.workers.RemoveDownloadWorker
import com.raywenderlich.emitron.ui.mytutorial.MyTutorialFragmentDirections
import com.raywenderlich.emitron.ui.onboarding.OnboardingView
import com.raywenderlich.emitron.ui.common.StartEndBottomMarginDecoration
import com.raywenderlich.emitron.ui.common.SwipeActionCallback
import com.raywenderlich.emitron.utils.extensions.setDataBindingView
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * Downloads view
 */
class DownloadFragment : DaggerFragment() {

  /**
   * Custom factory for viewmodel
   *
   * Custom factory provides app related dependencies
   */
  @Inject
  lateinit var viewModelFactory: ViewModelFactory

  /**
   * Download manager instance to observe download progress
   */
  @Inject
  lateinit var downloadManager: DownloadManager

  private val viewModel: DownloadViewModel by viewModels { viewModelFactory }

  internal val adapter by lazy {
    ContentAdapter.build(
      pagedAdapter = PagedAdapter(),
      onItemClick = ::openCollection,
      retryCallback = ::loadDownloads,
      emptyCallback = ::handleEmpty,
      onItemRetry = ::loadDownloads,
      adapterContentType = ContentAdapter.AdapterContentType.ContentDownloaded
    )
  }

  private val pagedFragment = lazy(LazyThreadSafetyMode.NONE) {
    ContentPagedFragment(
      viewModel.getPaginationViewModel(),
      adapter
    )
  }

  private lateinit var binding: FragmentDownloadsBinding

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
    initObservers()
    loadDownloads()
    checkAndShowOnboarding()
  }

  private fun initUi() {
    pagedFragment.value.initPaging(this, binding.recyclerView)
    binding.recyclerView.addItemDecoration(StartEndBottomMarginDecoration())
    addSwipeToDelete()
  }

  private fun addSwipeToDelete() {
    val swipeHandler =
      SwipeActionCallback.build(
        R.drawable.bg_swipe_bookmark,
        R.string.button_delete,
        onSwipe = {
          deleteDownload(adapter.getItemFor(it))
        })

    val itemTouchHelper = ItemTouchHelper(swipeHandler)
    itemTouchHelper.attachToRecyclerView(binding.recyclerView)
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

  private fun initObservers() {
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
}
