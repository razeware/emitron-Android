package com.razeware.emitron.ui.download

import androidx.lifecycle.LiveData
import com.razeware.emitron.data.download.DownloadRepository
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.DownloadState
import com.razeware.emitron.model.entity.Download
import com.razeware.emitron.model.entity.inProgress
import com.razeware.emitron.model.entity.isCompleted
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
  ):
      com.razeware.emitron.model.Download?

  /**
   * Update download progress
   *
   * @param contentId Content id
   * @param progress Int
   * @state Download state
   */
  suspend fun updateDownloadProgress(
    contentId: String,
    progress: Int,
    state: DownloadState
  )
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
    if (downloads.isEmpty()) {
      return null
    }

    val downloadProgress: Pair<Int, Int> = when {
      downloads.any { it.inProgress() } -> {
        downloads.map {
          it.progress
        }.reduce { acc, i ->
          i + acc
        } to DownloadState.IN_PROGRESS.ordinal
      }
      downloadIds.size == downloads.size && downloads.all { it.isCompleted() } -> {
        100 to DownloadState.COMPLETED.ordinal
      }
      else -> {
        0 to DownloadState.PAUSED.ordinal
      }
    }

    return com.razeware.emitron.model.Download(
      progress = downloadProgress.first,
      state = downloadProgress.second
    )
  }

  override suspend fun updateDownloadProgress(
    contentId: String,
    progress: Int,
    state: DownloadState
  ) {
    downloadRepository.updateDownloadProgress(contentId, progress, state)
  }
}
