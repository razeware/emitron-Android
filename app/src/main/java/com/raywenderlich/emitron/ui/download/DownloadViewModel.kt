package com.raywenderlich.emitron.ui.download

import androidx.lifecycle.ViewModel
import com.raywenderlich.emitron.data.download.DownloadRepository
import com.raywenderlich.emitron.data.settings.SettingsRepository
import com.raywenderlich.emitron.ui.content.ContentPagedViewModel
import com.raywenderlich.emitron.ui.onboarding.OnboardingView
import javax.inject.Inject

/**
 * ViewModel for downloads
 */
class DownloadViewModel @Inject constructor(
  private val downloadRepository: DownloadRepository,
  private val contentPagedViewModel: ContentPagedViewModel,
  private val settingsRepository: SettingsRepository
) : ViewModel() {

  /**
   * Load bookmarks from database
   */
  fun loadDownloads() {
    val listing = downloadRepository.getDownloads()
    contentPagedViewModel.localRepoResult.value = listing
  }

  /**
   * Get pagination helper
   */
  fun getPaginationViewModel(): ContentPagedViewModel = contentPagedViewModel

  fun isOnboardedForType(view: OnboardingView): Boolean =
    settingsRepository.getOnboardedViews().contains(view)

  fun isOnboardingAllowed(): Boolean = settingsRepository.isOnboardingAllowed()
}
