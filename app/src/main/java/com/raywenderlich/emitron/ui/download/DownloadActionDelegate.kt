package com.raywenderlich.emitron.ui.download

import androidx.lifecycle.LiveData
import com.raywenderlich.emitron.data.download.DownloadRepository
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.model.DownloadProgress
import com.raywenderlich.emitron.model.entity.Download
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
   * @param downloads list of [com.raywenderlich.emitron.model.entity.Download] from db
   *
   * @return Download state for collection
   */
  fun getCollectionDownloadState(
    collection: Data?,
    downloads: List<Download>,
    downloadIds: List<String>
  ):
      com.raywenderlich.emitron.model.Download?

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
      com.raywenderlich.emitron.model.Download? {

    val collectionId = collection?.id

    val collectionIsScreencast = collection?.isTypeScreencast() ?: false

    return if (collectionIsScreencast) {
      val download =
        downloads.first { it.downloadId == collectionId }.toDownloadState()
      download
    } else {
      getVideoCourseDownloadState(downloads)
    }
  }

  private fun getVideoCourseDownloadState(
    downloads: List<Download>
  ): com.raywenderlich.emitron.model.Download? {
    return com.raywenderlich.emitron.model.Download.fromEpisodeDownloads(downloads)
  }

  override suspend fun updateDownloadProgress(progress: DownloadProgress) {
    downloadRepository.updateDownloadProgress(progress)
  }
}
