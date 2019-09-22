package com.raywenderlich.emitron.ui.collection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.databinding.FragmentCollectionBinding
import com.raywenderlich.emitron.di.modules.viewmodel.ViewModelFactory
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.ui.common.ShimmerProgressDelegate
import com.raywenderlich.emitron.ui.content.getReadableContributors
import com.raywenderlich.emitron.ui.content.getReadableReleaseAtWithTypeAndDuration
import com.raywenderlich.emitron.ui.mytutorial.bookmarks.BookmarkActionDelegate
import com.raywenderlich.emitron.ui.mytutorial.progressions.ProgressionActionDelegate
import com.raywenderlich.emitron.utils.UiStateManager
import com.raywenderlich.emitron.utils.extensions.*
import com.raywenderlich.emitron.utils.getDefaultAppBarConfiguration
import dagger.android.support.DaggerFragment
import javax.inject.Inject

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

  private lateinit var progressDelegate: ShimmerProgressDelegate

  /**
   * See [androidx.fragment.app.Fragment.onCreateView]
   */
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = setDataBindingView(
      inflater,
      R.layout.fragment_collection,
      container
    )
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
  }

  private fun initUi() {
    binding.toolbar.setupWithNavController(findNavController(), getDefaultAppBarConfiguration())

    binding.textCollectionBodyPro.removeUnderline()

    episodeAdapter = CollectionEpisodeAdapter(
      onEpisodeSelected = { currentEpisode, _ ->
        if (viewModel.isFreeContent()) {
          openPlayer(currentEpisode)
        }
      },
      onEpisodeCompleted = { episode, position ->
        viewModel.updateContentProgression(episode, position)
      })

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
    progressDelegate = ShimmerProgressDelegate(requireView())
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
        if (it.isScreenCast()) {
          binding.groupCollectionContent.visibility = View.GONE
          binding.buttonCollectionPlay.toVisibility(true)
        }
      }
    }

    viewModel.loadCollectionResult.observe(viewLifecycleOwner) {
      if (it?.getContentIfNotHandled() == false) {
        showErrorSnackbar(getString(R.string.message_collection_episode_load_failed))
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
    if (showProgress) {
      progressDelegate.showProgressView()
      binding.groupCollectionContent.visibility = View.GONE
    } else {
      progressDelegate.hideProgressView()
    }
  }
}
