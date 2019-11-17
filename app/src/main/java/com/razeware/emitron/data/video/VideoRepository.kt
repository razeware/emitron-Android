package com.razeware.emitron.data.video

import com.razeware.emitron.model.Content
import com.razeware.emitron.utils.async.ThreadManager
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Repository for video playback actions
 */
class VideoRepository @Inject constructor(
  private val videoApi: VideoApi,
  private val threadManager: ThreadManager
) {

  /**
   * Get video stream
   */
  @Throws(Exception::class)
  suspend fun getVideoStream(id: String): Content {
    return withContext(threadManager.io) {
      videoApi.getVideoStream(id)
    }
  }

  /**
   * Get video playback token
   */
  @Throws(Exception::class)
  suspend fun getVideoPlaybackToken(): Content? {
    return withContext(threadManager.io) {
      videoApi.getPlaybackToken()
    }
  }
}
