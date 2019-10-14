package com.raywenderlich.emitron.ui.collection

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkManager
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.databinding.FragmentCollectionBinding
import com.raywenderlich.emitron.di.modules.viewmodel.ViewModelFactory
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.model.DownloadState
import com.raywenderlich.emitron.model.isScreencast
import com.raywenderlich.emitron.ui.common.getDefaultAppBarConfiguration
import com.raywenderlich.emitron.ui.content.getReadableContributors
import com.raywenderlich.emitron.ui.content.getReadableReleaseAtWithTypeAndDuration
import com.raywenderlich.emitron.ui.download.workers.StartDownloadWorker
import com.raywenderlich.emitron.ui.mytutorial.bookmarks.BookmarkActionDelegate
import com.raywenderlich.emitron.ui.mytutorial.progressions.ProgressionActionDelegate
import com.raywenderlich.emitron.ui.onboarding.OnboardingView
import com.raywenderlich.emitron.utils.UiStateManager
import com.raywenderlich.emitron.utils.createMainThreadScheduledHandler
import com.raywenderlich.emitron.utils.extensions.*
import dagger.android.support.DaggerFragment
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Collection detail view
 */
class CollectionFragment : DaggerFragment() {

  /**
   * Custom factory for viewmodel
   *
   * Custom factory provides app related dependencies
   */
  @Inject
  lateinit var viewModelFactory: ViewModelFactory

  private val viewModel: CollectionViewModel by viewModels { viewModelFactory }

  private val args by navArgs<CollectionFragmentArgs>()

  private lateinit var episodeAdapter: CollectionEpisodeAdapter

  private lateinit var binding: FragmentCollectionBinding

  companion object {
    /**
     * Handler interval to update download progress
     */
    const val downloadProgressUpdateIntervalMillis: Long = 5000L
  }

  /**
   * Download manager instance to observe download progress
   */
  @Inject
  lateinit var downloadManager: DownloadManager

  private var downloadProgressHandler: Handler? = null

  /**
   * Download listener
   */
  class DownloadStartListener(private val onDownloadStart: () -> Unit) : DownloadManager.Listener {

    /**
     * See [DownloadManager.Listener.onDownloadChanged]
     */
    override fun onDownloadChanged(downloadManager: DownloadManager?, download: Download?) {
      if (download?.state == Download.STATE_DOWNLOADING
        || download?.state == Download.STATE_RESTARTING
      ) {
        onDownloadStart()
      }
    }
  }

  private var downloadStartListener: DownloadStartListener? = null

  /**
   * See [androidx.fragment.app.Fragment.onCreateView]
   */
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = setDataBindingView(R.layout.fragment_collection, container)
    binding.data = viewModel.collection
    return binding.root
  }

  /**
   * See [androidx.fragment.app.Fragment.onViewCreated]
   */
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initUi()
    initObservers()
    loadCollection()
    checkAndShowOnboarding()
  }

  private fun initUi() {
    binding.toolbar.setupWithNavController(
      findNavController(),
      getDefaultAppBarConfiguration()
    )

    binding.textCollectionBodyPro.removeUnderline()

    episodeAdapter = CollectionEpisodeAdapter(
      onEpisodeSelected = { currentEpisode, _ ->
        if (viewModel.isFreeContent()) {
          openPlayer(currentEpisode)
        }
      },
      onEpisodeCompleted = { episode, position ->
        viewModel.updateContentProgression(episode, position)
      },
      onEpisodeDownload = { episode, _ ->
        startDownload(episode?.id)
      }
    )

    with(binding.recyclerViewCollectionEpisode) {
      layoutManager = object : LinearLayoutManager(requireContext()) {
        override fun canScrollVertically(): Boolean = false
      }
      adapter = episodeAdapter
    }

    binding.buttonCollectionBookmark.setOnClickListener {
      viewModel.updateContentBookmark()
    }

    binding.buttonCollectionPlay.setOnClickListener {
      openPlayer()
    }

    binding.buttonCollectionDownload.setOnClickListener {
      startDownload()
    }
  }

  private fun startDownload(episodeId: String? = null) {

    if (!viewModel.isDownloadAllowed()) {
      showErrorSnackbar(getString(R.string.error_download_permission))
      return
    }

    val contentId = viewModel.getContentId()
    contentId?.let {
      StartDownloadWorker.enqueue(
        WorkManager.getInstance(requireContext()),
        contentId,
        episodeId
      )
    }

    initDownloadProgressHandler()
  }

  private fun initObservers() {
    viewModel.collectionEpisodes.observe(viewLifecycleOwner) {
      it?.let {
        episodeAdapter.submitList(it)
        binding.groupCollectionContent.toVisibility(true)

        if (viewModel.isFreeContent()) {
          binding.buttonCollectionPlay.toVisibility(true)
        }
      }
    }

    viewModel.collection.observe(viewLifecycleOwner) {
      it?.let {
        val releaseDateWithTypeAndDuration = it.getReadableReleaseAtWithTypeAndDuration(
          requireContext(),
          withDifficulty = true,
          withYear = false
        )

        episodeAdapter.isProCourse = !it.isFreeContent()

        val contributors = it.getReadableContributors(requireContext())
        binding.textCollectionDuration.text = releaseDateWithTypeAndDuration
        binding.textCollectionAuthor.text = contributors
      }
    }

    viewModel.collectionContentType.observe(viewLifecycleOwner) {
      it?.let {
        if (it.isScreencast()) {
          binding.groupCollectionContent.visibility = View.GONE
          binding.buttonCollectionPlay.toVisibility(true)
        }
      }
    }

    viewModel.loadCollectionResult.observe(viewLifecycleOwner) {
      if (it?.getContentIfNotHandled() == false) {
        initDownloadObserver()
        showErrorSnackbar(getString(R.string.message_collection_episode_load_failed))
      } else {
        initDownloadObserver()
        initDownloadProgressHandler()
      }
    }

    viewModel.bookmarkActionResult.observe(viewLifecycleOwner) {
      when (it?.getContentIfNotHandled()) {
        BookmarkActionDelegate.BookmarkActionResult.BookmarkCreated -> {
          showSuccessSnackbar(getString(R.string.message_bookmark_created))
        }
        BookmarkActionDelegate.BookmarkActionResult.BookmarkFailedToCreate ->
          showErrorSnackbar(getString(R.string.message_bookmark_failed_to_create))
        BookmarkActionDelegate.BookmarkActionResult.BookmarkDeleted -> {
          showSuccessSnackbar(getString(R.string.message_bookmark_deleted))
        }
        BookmarkActionDelegate.BookmarkActionResult.BookmarkFailedToDelete ->
          showErrorSnackbar(getString(R.string.message_bookmark_failed_to_delete))
        null -> {
          // Houston, We Have a Problem!
        }
      }
    }

    viewModel.completionActionResult.observe(viewLifecycleOwner) {
      val (event, episodePosition) =
        it ?: (null to 0)
      when (event?.getContentIfNotHandled()) {
        ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeMarkedCompleted -> {
          showSuccessSnackbar(getString(R.string.message_episode_marked_completed))
        }
        ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeMarkedInProgress ->
          showSuccessSnackbar(getString(R.string.message_episode_marked_in_progress))
        ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeFailedToMarkComplete -> {
          episodeAdapter.updateEpisodeCompletion(episodePosition)
          showErrorSnackbar(getString(R.string.message_episode_failed_to_mark_completed))
        }
        ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeFailedToMarkInProgress -> {
          episodeAdapter.updateEpisodeCompletion(episodePosition)
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

    viewModel.uiState.observe(viewLifecycleOwner) {
      when (it) {
        UiStateManager.UiState.LOADED -> handleProgress(false)
        UiStateManager.UiState.LOADING -> handleProgress(true)
        else -> {
          // Ignored, for now :)
        }
      }
    }

  }

  private fun initDownloadObserver() {
    viewModel.getDownloads(viewModel.getContentIds())
      .observe(viewLifecycleOwner) { downloads ->
        downloads?.let {
          if (it.isNotEmpty()) {
            val collectionDownload = viewModel.getCollectionDownloadState(downloads)
            binding.buttonCollectionDownload.updateDownloadState(collectionDownload)
            episodeAdapter.updateEpisodeDownloadProgress(downloads)
          }
        }
      }
  }

  private fun loadCollection() {
    args.collection?.let {
      viewModel.loadCollection(it)
    }
  }

  private fun openPlayer(currentEpisode: Data? = null) {
    val playList = viewModel.getPlaylist()
    val playlistWithSelectedEpisode = playList.copy(currentEpisode = currentEpisode)
    val action =
      CollectionFragmentDirections.actionNavigationCollectionToNavigationPlayer(
        playlistWithSelectedEpisode
      )
    findNavController().navigate(action)
  }

  private fun handleProgress(showProgress: Boolean = false) {
    with(binding) {
      groupEpisodeProgress.toVisibility(showProgress)
      if (showProgress) {
        groupCollectionContent.toVisibility(!showProgress)
      }
    }
  }

  private fun checkAndShowOnboarding() {
    val canShowCollectionOnboarding =
      viewModel.isOnboardingAllowed() &&
          !viewModel.isOnboardedForType(OnboardingView.Collection)

    if (canShowCollectionOnboarding) {
      val action = CollectionFragmentDirections.actionNavigationCollectionToNavigationOnboarding(
        OnboardingView.Collection
      )
      findNavController().navigate(action)
    }
  }

  private fun initDownloadProgressHandler() {
    val downloads = downloadManager.currentDownloads

    if (!downloads.isNullOrEmpty()) {
      val downloadIds = downloads.map {
        it.request.id
      }.filter {
        it in viewModel.getContentIds()
      }

      if (downloadIds.isNotEmpty()) {
        createDownloadProgressHandler()
      }
    }
    if (null == downloadStartListener) {
      downloadStartListener = DownloadStartListener {
        createDownloadProgressHandler()
      }
      downloadManager.addListener(downloadStartListener)
    }
  }

  private fun updateDownloadProgress() {
    val downloads = downloadManager.currentDownloads

    if (downloads.isEmpty()) {
      downloadProgressHandler?.removeCallbacksAndMessages(null)
      return
    }

    downloads.map {
      val state = when {
        it.state == Download.STATE_FAILED -> DownloadState.FAILED
        it.state == Download.STATE_COMPLETED -> DownloadState.COMPLETED
        it.state == Download.STATE_DOWNLOADING -> DownloadState.IN_PROGRESS
        it.state == Download.STATE_QUEUED -> DownloadState.PAUSED
        it.state == Download.STATE_STOPPED -> DownloadState.PAUSED
        else -> DownloadState.IN_PROGRESS
      }
      viewModel.updateDownload(
        it.request.id,
        it.percentDownloaded.roundToInt(),
        state
      )
    }
  }

  /**
   * See [androidx.fragment.app.Fragment.onDestroy]
   */
  override fun onDestroy() {
    super.onDestroy()
    downloadProgressHandler?.removeCallbacksAndMessages(null)
  }

  private fun createDownloadProgressHandler() {
    if (null == downloadProgressHandler) {
      downloadProgressHandler =
        createMainThreadScheduledHandler(
          requireContext(),
          downloadProgressUpdateIntervalMillis
        ) {
          updateDownloadProgress()
        }
    }
  }
}
