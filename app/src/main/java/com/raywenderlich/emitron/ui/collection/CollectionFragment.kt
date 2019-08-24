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
import com.raywenderlich.emitron.ui.common.ShimmerProgressDelegate
import com.raywenderlich.emitron.ui.content.getReadableContributors
import com.raywenderlich.emitron.ui.content.getReadableReleaseAtWithTypeAndDuration
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

  private val adapter = CollectionEpisodeAdapter(
    onEpisodeSelected = { currentEpisode, nextEpisode ->
      openPlayer()
    },
    onEpisodeCompleted = { episode, position ->
      viewModel.toggleEpisodeCompleted(episode, position)
    })

  private lateinit var binding: FragmentCollectionBinding

  private val progressDelegate by lazy(LazyThreadSafetyMode.NONE) {
    ShimmerProgressDelegate(requireView())
  }

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

    with(binding.recyclerViewCollectionEpisode) {
      layoutManager = object : LinearLayoutManager(requireContext()) {
        override fun canScrollVertically(): Boolean = false
      }
      adapter = this@CollectionFragment.adapter
    }

    binding.buttonCollectionBookmark.setOnClickListener {
      viewModel.toggleBookmark()
    }

    binding.buttonCollectionPlay.setOnClickListener {
      openPlayer()
    }
  }

  private fun initObservers() {
    viewModel.episodeEpisodes.observe(viewLifecycleOwner) {
      it?.let {
        adapter.submitList(it)
        adapter.notifyDataSetChanged()
        handleProgress()
      }
    }

    viewModel.collection.observe(viewLifecycleOwner) {
      it?.let {
        val releaseDateWithTypeAndDuration = it.getReadableReleaseAtWithTypeAndDuration(
          requireContext(),
          withDifficulty = true,
          withYear = false
        )

        adapter.isProCourse = !it.isFreeContent()

        val contributors = it.getReadableContributors(requireContext())
        binding.textCollectionDuration.text = releaseDateWithTypeAndDuration
        binding.textCollectionAuthor.text = contributors
      }
    }

    viewModel.contentType.observe(viewLifecycleOwner) {
      it?.let {
        if (it.isScreenCast()) {
          binding.groupCollectionContent.visibility = View.GONE
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
        CollectionViewModel.BookmarkActionResult.BookmarkCreated -> {
          showSuccessSnackbar(getString(R.string.message_bookmark_created))
        }
        CollectionViewModel.BookmarkActionResult.BookmarkFailedToCreate ->
          showErrorSnackbar(getString(R.string.message_bookmark_failed_to_create))
        CollectionViewModel.BookmarkActionResult.BookmarkDeleted -> {
          showSuccessSnackbar(getString(R.string.message_bookmark_deleted))
        }
        CollectionViewModel.BookmarkActionResult.BookmarkFailedToDelete ->
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
        CollectionViewModel.EpisodeProgressionActionResult.EpisodeMarkedCompleted -> {
          showSuccessSnackbar(getString(R.string.message_episode_marked_completed))
        }
        CollectionViewModel.EpisodeProgressionActionResult.EpisodeMarkedInProgress ->
          showSuccessSnackbar(getString(R.string.message_episode_marked_in_progress))
        CollectionViewModel.EpisodeProgressionActionResult.EpisodeFailedToMarkComplete -> {
          adapter.updateEpisodeCompletion(episodePosition)
          showErrorSnackbar(getString(R.string.message_episode_failed_to_mark_completed))
        }
        CollectionViewModel.EpisodeProgressionActionResult.EpisodeFailedToMarkInProgress -> {
          adapter.updateEpisodeCompletion(episodePosition)
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

  private fun loadCollection() {
    args.collection?.let {
      handleProgress(true)
      viewModel.loadCollection(it)
    }
  }

  private fun openPlayer() {
    // Open player
  }

  private fun handleProgress(showProgress: Boolean = false) {
    if (showProgress) {
      progressDelegate.showProgressView()
      binding.groupCollectionContent.visibility = View.GONE
    } else {
      binding.groupCollectionContent.visibility = View.VISIBLE
      progressDelegate.hideProgressView()
    }
  }

}
