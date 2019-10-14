package com.raywenderlich.emitron.ui.download

import androidx.lifecycle.LiveData
import com.raywenderlich.emitron.data.download.DownloadRepository
import com.raywenderlich.emitron.data.login.LoginRepository
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.model.DownloadState
import com.raywenderlich.emitron.model.entity.Download
import com.raywenderlich.emitron.model.entity.inProgress
import com.raywenderlich.emitron.model.entity.isCompleted
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
  fun getCollectionDownloadState(collection: Data?, downloads: List<Download>):
      com.raywenderlich.emitron.model.Download?

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

  /**
   * @return true if downloads can be shown, else false
   */
  fun isDownloadAllowed(): Boolean
}

/**
 * [DownloadAction] implementation
 */
class DownloadActionDelegate @Inject constructor(
  private val downloadRepository: DownloadRepository,
  private val loginRepository: LoginRepository
) : DownloadAction {

  override fun getDownloads(downloadIds: List<String>): LiveData<List<Download>> {
    return downloadRepository.getDownloadsById(downloadIds)
  }

  override fun getCollectionDownloadState(collection: Data?, downloads: List<Download>):
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
    downloads:
    List<Download>
  ): com.raywenderlich.emitron.model.Download? {
    return if (downloads.isNotEmpty()) {
      val downloadProgress: Pair<Int, Int> = when {
        downloads.any { it.inProgress() } -> {
          downloads.map {
            it.progress
          }.reduce { acc, i ->
            i + acc
          } to DownloadState.IN_PROGRESS.ordinal
        }
        downloads.all { it.isCompleted() } -> {
          100 to DownloadState.COMPLETED.ordinal
        }
        else -> {
          0 to DownloadState.IN_PROGRESS.ordinal
        }
      }

      com.raywenderlich.emitron.model.Download(
        progress = downloadProgress.first,
        state = downloadProgress.second
      )
    } else {
      null
    }
  }

  override suspend fun updateDownloadProgress(
    contentId: String,
    progress: Int,
    state: DownloadState
  ) {
    downloadRepository.updateDownloadProgress(contentId, progress, state)
  }


  /**
   * @return true if downloads can be shown, else false
   */
  override fun isDownloadAllowed(): Boolean = loginRepository.hasDownloadPermission()
}
