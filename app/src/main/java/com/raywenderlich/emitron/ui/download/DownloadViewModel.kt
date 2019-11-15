package com.raywenderlich.emitron.ui.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raywenderlich.emitron.data.download.DownloadRepository
import com.raywenderlich.emitron.model.DownloadProgress
import com.raywenderlich.emitron.ui.content.ContentPagedViewModel
import com.raywenderlich.emitron.ui.login.PermissionActionDelegate
import com.raywenderlich.emitron.ui.login.PermissionsAction
import com.raywenderlich.emitron.ui.onboarding.OnboardingAction
import com.raywenderlich.emitron.ui.onboarding.OnboardingActionDelegate
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for downloads
 */
class DownloadViewModel @Inject constructor(
  private val downloadRepository: DownloadRepository,
  private val contentPagedViewModel: ContentPagedViewModel,
  private val onboardingActionDelegate: OnboardingActionDelegate,
  private val permissionActionDelegate: PermissionActionDelegate,
  private val downloadActionDelegate: DownloadActionDelegate
) : ViewModel(), OnboardingAction by onboardingActionDelegate,
  PermissionsAction by permissionActionDelegate, DownloadAction by downloadActionDelegate {

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

  /**
   * Update download progress
   *
   * @param downloadProgress Download progress
   */
  fun updateDownload(downloadProgress: DownloadProgress) {
    viewModelScope.launch {
      downloadActionDelegate.updateDownloadProgress(downloadProgress)
    }
  }
}
