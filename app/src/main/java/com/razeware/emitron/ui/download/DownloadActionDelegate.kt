package com.razeware.emitron.ui.download

import androidx.lifecycle.LiveData
import com.razeware.emitron.data.download.DownloadRepository
import com.razeware.emitron.data.settings.SettingsRepository
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.Download
import com.razeware.emitron.model.DownloadProgress
import javax.inject.Inject

/**
 * Download UI actions
 */
interface DownloadAction {

  /**
   * Get downloads by id
   *
   * @param downloadIds Download ids
   *
   * @return Observable for Downloads by id
   */
  fun getDownloads(downloadIds: List<String>):
      LiveData<List<com.razeware.emitron.model.entity.Download>>

  /**
   * Collection download state
   *
   * @param downloads list of [com.razeware.emitron.model.entity.Download] from db
   *
   * @return Download state for collection
   */
  fun getCollectionDownloadState(
    collection: Data?,
    downloads: List<com.razeware.emitron.model.entity.Download>,
    downloadIds: List<String>
  ): Download?

  /**
   * Update download progress
   *
   * @param progress [DownloadProgress]
   */
  suspend fun updateDownloadProgress(progress: DownloadProgress)

  /**
   * Should only download only on wifi
   */
  fun downloadsWifiOnly(): Boolean
}

/**
 * [DownloadAction] implementation
 */
class DownloadActionDelegate @Inject constructor(
  private val downloadRepository: DownloadRepository,
  private val settingsRepository: SettingsRepository
) : DownloadAction {

  override fun getDownloads(downloadIds: List<String>):
      LiveData<List<com.razeware.emitron.model.entity.Download>> {
    return downloadRepository.getDownloadsById(downloadIds)
  }

  override fun getCollectionDownloadState(
    collection: Data?,
    downloads: List<com.razeware.emitron.model.entity.Download>,
    downloadIds: List<String>
  ): Download? {

    val collectionId = collection?.id

    val collectionIsScreencast = collection?.isTypeScreencast() ?: false

    return if (collectionIsScreencast) {
      val download =
        downloads.first { it.downloadId == collectionId }.toDownloadState()
      download
    } else {
      getVideoCourseDownloadState(downloads, downloadIds)
    }
  }

  private fun getVideoCourseDownloadState(
    downloads: List<com.razeware.emitron.model.entity.Download>,
    downloadIds: List<String>
  ): Download? {

    if (downloads.isEmpty()) {
      return null
    }

    return Download.fromEpisodeDownloads(downloads, downloadIds)
  }

  override suspend fun updateDownloadProgress(progress: DownloadProgress) {
    downloadRepository.updateDownloadProgress(progress)
  }

  override fun downloadsWifiOnly(): Boolean = settingsRepository.getDownloadsWifiOnly()
}
