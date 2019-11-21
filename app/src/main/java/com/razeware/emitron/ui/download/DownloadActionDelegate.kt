package com.razeware.emitron.ui.download

import androidx.lifecycle.LiveData
import com.razeware.emitron.data.download.DownloadRepository
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.DownloadProgress
import com.razeware.emitron.model.entity.Download
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
  fun getDownloads(downloadIds: List<String>): LiveData<List<Download>>

  /**
   * Collection download state
   *
   * @param downloads list of [com.razeware.emitron.model.entity.Download] from db
   *
   * @return Download state for collection
   */
  fun getCollectionDownloadState(
    collection: Data?,
    downloads: List<Download>,
    downloadIds: List<String>
  ): com.razeware.emitron.model.Download?

  /**
   * Update download progress
   *
   * @param progress [DownloadProgress]
   */
  suspend fun updateDownloadProgress(progress: DownloadProgress)
}

/**
 * [DownloadAction] implementation
 */
class DownloadActionDelegate @Inject constructor(
  private val downloadRepository: DownloadRepository
) : DownloadAction {

  override fun getDownloads(downloadIds: List<String>): LiveData<List<Download>> {
    return downloadRepository.getDownloadsById(downloadIds)
  }

  override fun getCollectionDownloadState(
    collection: Data?,
    downloads: List<Download>,
    downloadIds: List<String>
  ):
      com.razeware.emitron.model.Download? {

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
    downloads: List<Download>,
    downloadIds: List<String>
  ): com.razeware.emitron.model.Download? {
    return com.razeware.emitron.model.Download.fromEpisodeDownloads(downloads, downloadIds)
  }

  override suspend fun updateDownloadProgress(progress: DownloadProgress) {
    downloadRepository.updateDownloadProgress(progress)
  }
}
