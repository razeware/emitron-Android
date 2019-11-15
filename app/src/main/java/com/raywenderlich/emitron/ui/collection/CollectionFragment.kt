package com.raywenderlich.emitron.ui.collection

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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
import com.raywenderlich.emitron.ui.download.workers.RemoveDownloadWorker
import com.raywenderlich.emitron.ui.download.workers.StartDownloadWorker
import com.raywenderlich.emitron.ui.login.PermissionActionDelegate
import com.raywenderlich.emitron.ui.mytutorial.bookmarks.BookmarkActionDelegate
import com.raywenderlich.emitron.ui.mytutorial.progressions.ProgressionActionDelegate
import com.raywenderlich.emitron.ui.onboarding.OnboardingView
import com.raywenderlich.emitron.ui.player.workers.UpdateOfflineProgressWorker
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

  private var verifyDownloadDialog: AlertDialog? = null

  private var removeDownloadDialog: AlertDialog? = null

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
        if (viewModel.isContentPlaybackAllowed(isNetConnected())) {
          openPlayer(currentEpisode)
        } else {
          if (viewModel.isDownloaded()) {
            showVerifyDownloadBottomSheet(currentEpisode)
          }
        }
      },
      onEpisodeCompleted = { episode, position ->
        viewModel.updateContentProgression(isNetConnected(), episode, position)
      },
      onEpisodeDownload = { episode, _ ->
        startDownload(episode?.id, episode?.isDownloaded() == true)
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

  private fun startDownload(episodeId: String? = null, episodeIsDownloaded: Boolean = false) {

    if (!viewModel.isDownloadAllowed()) {
      showErrorSnackbar(getString(R.string.message_download_permission_error))
      return
    }

    val contentId = viewModel.getContentId() ?: return

    // Delete downloaded episode
    if (!episodeId.isNullOrBlank() && episodeIsDownloaded) {
      showDeleteDownloadedContentDialog(episodeId)
      return
    }

    // Delete downloaded collection
    if (viewModel.isDownloaded()) {
      showDeleteDownloadedContentDialog(contentId, true)
      return
    }

    StartDownloadWorker.enqueue(
      WorkManager.getInstance(requireContext()),
      contentId,
      episodeId
    )

    initDownloadProgressHandler()
  }

  private fun showDeleteDownloadedContentDialog(downloadId: String, isCollection: Boolean = false) {
    if (isShowingRemoveDownloadDialog()) {
      return
    }
    removeDownloadDialog = createDialog(
      title = R.string.title_download_remove,
      message = R.string.message_download_remove,
      positiveButton = R.string.button_label_yes,
      positiveButtonClickListener = {
        handleRemoveDownload(downloadId, isCollection)
      },
      negativeButton = R.string.button_label_no
    )
    removeDownloadDialog?.show()
  }

  private fun handleRemoveDownload(downloadId: String, isCollection: Boolean = false) {
    if (isCollection) {
      binding.buttonCollectionDownload.updateDownloadState(null)
      episodeAdapter.removeEpisodeDownload(viewModel.getContentIds())
    } else {
      episodeAdapter.removeEpisodeDownload(listOf(downloadId))
    }
    RemoveDownloadWorker.enqueue(
      WorkManager.getInstance(requireContext()),
      downloadId
    )
  }

  private fun initObservers() {
    viewModel.collectionEpisodes.observe(viewLifecycleOwner) {
      it?.let {
        episodeAdapter.submitList(it)
        val playbackAllowed = viewModel.isContentPlaybackAllowed(isNetConnected())
        with(binding) {
          groupCollectionContent.toVisibility(true)
          groupProfessionalContent.toVisibility(!playbackAllowed)
          buttonCollectionPlay.toVisibility(playbackAllowed)
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

        episodeAdapter.updateContentPlaybackAllowed(
          viewModel.isContentPlaybackAllowed(isConnected = true)
        )

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
      handleProgress(UiStateManager.UiState.LOADING == it)
    }

    /**
     * Add observer to enqueue sync-back for offline progressions update
     */
    viewModel.enqueueOfflineProgressUpdate.observe(viewLifecycleOwner) {
      it?.let {
        UpdateOfflineProgressWorker.enqueue(WorkManager.getInstance(requireContext()))
      }
    }
  }

  private fun initDownloadObserver() {
    val downloadIds = viewModel.getContentIds()
    viewModel.getDownloads(downloadIds)
      .observe(viewLifecycleOwner) { downloads ->
        downloads?.let {
          if (it.isNotEmpty()) {
            val collectionDownload = viewModel.updateCollectionDownloadState(downloads, downloadIds)
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
      }.intersect(viewModel.getContentIds())

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

    val downloadIds = downloads.map {
      it.request.id
    }.intersect(viewModel.getContentIds())

    if (downloadIds.isEmpty()) {
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
   * See [androidx.fragment.app.Fragment.onDestroyView]
   */
  override fun onDestroyView() {
    super.onDestroyView()
    if (isShowingVerifyDownloadDialog()) {
      verifyDownloadDialog?.dismiss()
    }
    if (isShowingRemoveDownloadDialog()) {
      removeDownloadDialog?.dismiss()
    }
    downloadProgressHandler?.removeCallbacksAndMessages(null)
  }

  private fun createDownloadProgressHandler() {
    if (null == downloadProgressHandler && isVisible) {
      downloadProgressHandler =
        createMainThreadScheduledHandler(
          requireContext(),
          downloadProgressUpdateIntervalMillis
        ) {
          updateDownloadProgress()
        }
    }
  }

  private fun showVerifyDownloadBottomSheet(currentEpisode: Data? = null) {
    if (isShowingVerifyDownloadDialog()) {
      return
    }

    verifyDownloadDialog = createDialog(
      title = R.string.title_download_permission_error,
      message = R.string.message_download_permission_error,
      positiveButton = R.string.button_label_play_online,
      positiveButtonClickListener = {
        if (viewModel.isContentPlaybackAllowed(isNetConnected(), false)) {
          openPlayer(currentEpisode)
        }
      },
      negativeButton = R.string.button_label_verify,
      negativeButtonClickListener = {
        viewModel.getPermissions()
        initPermissionObserver(currentEpisode)
      }
    )
    verifyDownloadDialog?.show()
  }

  private fun isShowingVerifyDownloadDialog() = verifyDownloadDialog?.isShowing == true

  private fun isShowingRemoveDownloadDialog() = removeDownloadDialog?.isShowing == true

  private fun initPermissionObserver(currentEpisode: Data? = null) {
    viewModel.permissionActionResult.observe(viewLifecycleOwner) {
      when (it) {
        PermissionActionDelegate.PermissionActionResult.HasDownloadPermission -> {
          episodeAdapter.updateContentPlaybackAllowed(
            viewModel.isContentPlaybackAllowed(isConnected = true),
            refresh = true
          )
          openPlayer(currentEpisode)
        }
        PermissionActionDelegate.PermissionActionResult.NoPermission -> {
        }
        PermissionActionDelegate.PermissionActionResult.PermissionRequestFailed -> {
          RemoveDownloadWorker.enqueue(WorkManager.getInstance(requireContext()))
          showErrorSnackbar(getString(R.string.error_permission))
        }
        else -> {
          // Will be handled by data binding.
        }
      }
    }
  }
}
