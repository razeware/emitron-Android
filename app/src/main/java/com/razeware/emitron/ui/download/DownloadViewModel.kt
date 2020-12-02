package com.razeware.emitron.ui.download

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.razeware.emitron.data.download.DownloadRepository
import com.razeware.emitron.model.DownloadProgress
import com.razeware.emitron.ui.content.ContentPagedViewModel
import com.razeware.emitron.ui.login.PermissionActionDelegate
import com.razeware.emitron.ui.login.PermissionsAction
import com.razeware.emitron.ui.onboarding.OnboardingAction
import com.razeware.emitron.ui.onboarding.OnboardingActionDelegate
import kotlinx.coroutines.launch

/**
 * ViewModel for downloads
 */
class DownloadViewModel @ViewModelInject constructor(
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
