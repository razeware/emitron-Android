package com.raywenderlich.emitron.ui.download

import androidx.lifecycle.ViewModel
import com.raywenderlich.emitron.data.download.DownloadRepository
import com.raywenderlich.emitron.ui.content.ContentPagedViewModel
import com.raywenderlich.emitron.ui.login.PermissionActionDelegate
import com.raywenderlich.emitron.ui.login.PermissionsAction
import com.raywenderlich.emitron.ui.onboarding.OnboardingAction
import com.raywenderlich.emitron.ui.onboarding.OnboardingActionDelegate
import javax.inject.Inject

/**
 * ViewModel for downloads
 */
class DownloadViewModel @Inject constructor(
  private val downloadRepository: DownloadRepository,
  private val contentPagedViewModel: ContentPagedViewModel,
  private val onboardingActionDelegate: OnboardingActionDelegate,
  private val permissionActionDelegate: PermissionActionDelegate
) : ViewModel(), OnboardingAction by onboardingActionDelegate,
  PermissionsAction by permissionActionDelegate {

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
}
