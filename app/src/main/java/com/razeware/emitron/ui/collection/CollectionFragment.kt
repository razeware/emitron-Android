package com.razeware.emitron.ui.collection

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import androidx.work.WorkManager
import com.google.android.exoplayer2.offline.DownloadManager
import com.raywenderlich.android.inappreview.InAppReviewView
import com.raywenderlich.android.inappreview.dialog.InAppReviewPromptDialog
import com.raywenderlich.android.inappreview.manager.InAppReviewManager
import com.razeware.emitron.R
import com.razeware.emitron.databinding.FragmentCollectionBinding
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.isScreencast
import com.razeware.emitron.ui.common.getDefaultAppBarConfiguration
import com.razeware.emitron.ui.content.getReadableContributors
import com.razeware.emitron.ui.content.getReadableReleaseAtWithTypeAndDuration
import com.razeware.emitron.ui.download.helpers.DownloadHelper
import com.razeware.emitron.ui.download.helpers.DownloadProgressHelper
import com.razeware.emitron.ui.download.workers.RemoveDownloadWorker
import com.razeware.emitron.ui.login.PermissionActionDelegate
import com.razeware.emitron.ui.mytutorial.bookmarks.BookmarkActionDelegate
import com.razeware.emitron.ui.mytutorial.progressions.ProgressionActionDelegate
import com.razeware.emitron.ui.onboarding.OnboardingView
import com.razeware.emitron.ui.player.workers.UpdateOfflineProgressWorker
import com.razeware.emitron.utils.UiStateManager
import com.razeware.emitron.utils.extensions.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Collection detail view
 */
@AndroidEntryPoint
class CollectionFragment : Fragment(), InAppReviewView {

  private val viewModel: CollectionViewModel by viewModels()

  private val args by navArgs<CollectionFragmentArgs>()

  private lateinit var episodeAdapter: CollectionEpisodeAdapter

  private lateinit var binding: FragmentCollectionBinding

  private var verifyDownloadDialog: AlertDialog? = null
  private var downloadMeteredNetworkErrorDialog: AlertDialog? = null

  /**
   * Helper to observer download progress from [DownloadManager]
   */
  @Inject
  lateinit var downloadProgressHelper: DownloadProgressHelper

  /**
   * Used to launch the In App Review flow if the user should see it.
   * */
  @Inject
  lateinit var reviewManager: InAppReviewManager

  private val downloadHelper: DownloadHelper by lazy {
    DownloadHelper(this)
  }

  /**
   * See [androidx.fragment.app.Fragment.onCreateView]
   */
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = setDataBindingView(R.layout.fragment_collection, container)
    binding.data = viewModel.collection
    binding.isDownloadAvailable = viewModel.isDownloadAllowed()
    return binding.root
  }

  /**
   * See [androidx.fragment.app.Fragment.onViewCreated]
   */
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.setInAppReviewView(this)
    initUi()
    initObservers()
    loadCollection()
    checkAndShowOnboarding()
  }

  private fun initUi() {
    with(binding) {
      toolbar.setupWithNavController(
        findNavController(),
        getDefaultAppBarConfiguration()
      )
      toolbar.navigationIcon =
        VectorDrawableCompat.create(resources, R.drawable.ic_arrow_back, null)

      textCollectionBodyPro.removeUnderline()

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
          viewModel.updateCollectionProgressionState()
          viewModel.updateContentProgression(isNetConnected(), episode, position)
        },
        onEpisodeDownload = { episode, _ ->
          handleDownload(episode?.id, episode?.isDownloaded() == true)
        },
        viewModel = viewModel
      )

      with(recyclerViewCollectionEpisode) {
        layoutManager = object : LinearLayoutManager(requireContext()) {
          override fun canScrollVertically(): Boolean = false
        }
        adapter = episodeAdapter
      }

      buttonCollectionBookmark.setOnClickListener {
        viewModel.updateContentBookmark()
      }

      buttonCollectionPlay.setOnClickListener {
        openPlayer()
      }

      buttonCollectionResume.setOnClickListener {
        openPlayer()
      }

      buttonCollectionDownload.setOnClickListener {
        handleDownload()
      }

      buttonManageSubscription.setOnClickListener {
        launchCustomTab(Uri.parse(getString(R.string.manage_subscription_url)))
      }
    }
  }

  private fun handleDownload(episodeId: String? = null, episodeIsDownloaded: Boolean = false) {
    val contentIsDownloaded = viewModel.isDownloaded()
    downloadHelper.startDownload(
      viewModel.isDownloadAllowed(),
      viewModel.getContentId(),
      contentIsDownloaded,
      episodeId,
      episodeIsDownloaded,
      {
        initDownloadProgress()

        if (isActiveNetworkMetered() && viewModel.downloadsWifiOnly()) {
          showDownloadMeteredNetworkErrorDialog()
        } else {
          showSuccessSnackbar(getString(R.string.message_download_started))
        }
      }, { downloadId ->
        if (contentIsDownloaded) {
          binding.buttonCollectionDownload.updateDownloadState(null)
          viewModel.removeDownload()
        } else {
          viewModel.removeEpisodeDownload(downloadId)
        }
      },
      viewModel.downloadsWifiOnly()
    )
  }

  private fun showDownloadMeteredNetworkErrorDialog() {
    downloadMeteredNetworkErrorDialog = createDialog(
      title = R.string.title_download_wifi_only_error,
      message = R.string.body_download_wifi_only_error,
      positiveButton = R.string.button_label_settings,
      positiveButtonClickListener = {
        openSettings()
        downloadMeteredNetworkErrorDialog?.dismiss()
      },
      negativeButton = R.string.button_label_dismiss,
      negativeButtonClickListener = {
        downloadMeteredNetworkErrorDialog?.dismiss()
      }
    )
    downloadMeteredNetworkErrorDialog?.show()
  }

  private fun openSettings() {
    findNavController().navigate(
      CollectionFragmentDirections.actionNavigationCollectionToNavigationSettings()
    )
  }

  private fun initObservers() {
    viewModel.collectionEpisodes.observe(viewLifecycleOwner) {
      episodeAdapter.update(it)
    }

    viewModel.loadCollectionEpisodesResult.observe(viewLifecycleOwner) {
      it?.let {
        val isSuccessFul = it.getContentIfNotHandled() ?: false
        if (isSuccessFul) {
          val playbackAllowed = viewModel.isContentPlaybackAllowed(isNetConnected())
          val hasProgress = viewModel.hasProgress()
          with(binding) {
            groupCollectionContent.isVisible = true
            groupProfessionalContent.isVisible = !playbackAllowed
            buttonCollectionPlay.isVisible = playbackAllowed && !hasProgress
            buttonCollectionResume.isVisible = playbackAllowed && hasProgress
            progressCompletion.isVisible = playbackAllowed && hasProgress
            progressCompletion.progress = viewModel.getProgress()
          }
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

        val contributors = it.getReadableContributors(requireContext())
        binding.textCollectionDuration.text = releaseDateWithTypeAndDuration
        binding.textCollectionAuthor.text = contributors
        if (it.isTypeScreencast()) {
          binding.progressCompletion.progress = viewModel.getProgress()
        }
        binding.textCollectionCompleted.isVisible = it.isProgressionFinished()
      }
    }

    viewModel.collectionContentType.observe(viewLifecycleOwner) {
      it?.let {
        if (it.isScreencast()) {
          with(binding) {
            groupCollectionContent.visibility = View.GONE
            buttonCollectionPlay.isVisible = !viewModel.hasProgress()
            buttonCollectionResume.isVisible = viewModel.hasProgress()
            progressCompletion.isVisible = viewModel.hasProgress()
            progressCompletion.progress = viewModel.getProgress()
          }
        }
      }
    }

    viewModel.loadCollectionResult.observe(viewLifecycleOwner) {
      if (it?.getContentIfNotHandled() == false) {
        initDownloadObserver()
        showErrorSnackbar(getString(R.string.message_collection_episode_load_failed))
      } else {
        initDownloadObserver()
        initDownloadProgress()
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
          viewModel.updateEpisodeProgression(false, episodePosition)
          showErrorSnackbar(getString(R.string.message_episode_failed_to_mark_completed))
        }
        ProgressionActionDelegate.EpisodeProgressionActionResult.EpisodeFailedToMarkInProgress -> {
          viewModel.updateEpisodeProgression(true, episodePosition)
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
      // Update Collection state as per new episode state
      viewModel.updateCollectionProgressionState()
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
            val collectionDownload =
              viewModel.updateCollectionDownloadState(downloads, downloadIds)
            binding.buttonCollectionDownload.updateDownloadState(collectionDownload)
            viewModel.updateEpisodeDownloadProgress(downloads)
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
    if (hasNougat()) {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
    val action =
      CollectionFragmentDirections.actionNavigationCollectionToNavigationPlayer(
        playlistWithSelectedEpisode
      )
    findNavController().navigate(action)
  }

  private fun handleProgress(showProgress: Boolean = false) {
    with(binding) {
      groupEpisodeProgress.isVisible = showProgress
      if (showProgress) {
        groupCollectionContent.isVisible = !showProgress
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

  /**
   * Checks if the user is eligible for a review prompt, and shows a dialog if needed.
   * */
  override fun showReviewFlow() {
    if (reviewManager.isEligibleForReview()) {
      val dialog = InAppReviewPromptDialog()

      dialog.show(childFragmentManager, null)
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
    if (isShowingDownloadMeteredNetworkErrorDialog()) {
      downloadMeteredNetworkErrorDialog?.dismiss()
    }

    downloadProgressHelper.clear()
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

  private fun isShowingDownloadMeteredNetworkErrorDialog() =
    downloadMeteredNetworkErrorDialog?.isShowing == true

  private fun initPermissionObserver(currentEpisode: Data? = null) {
    viewModel.permissionActionResult.observe(viewLifecycleOwner) {
      when (it) {
        PermissionActionDelegate.PermissionActionResult.HasDownloadPermission -> {
          episodeAdapter.notifyDataSetChanged()
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

  private fun initDownloadProgress() {
    downloadProgressHelper.init(isVisible, requireContext(), viewModel.getContentIds()) {
      viewModel.updateDownload(it)
    }
  }
}
