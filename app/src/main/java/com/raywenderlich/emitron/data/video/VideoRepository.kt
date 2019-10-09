package com.raywenderlich.emitron.data.video

import com.raywenderlich.emitron.model.Content
import com.raywenderlich.emitron.model.PlaybackProgress
import com.raywenderlich.emitron.utils.async.ThreadManager
import kotlinx.coroutines.withContext
import retrofit2.Response
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
  suspend fun getVideoPlaybackToken(): Content {
    return withContext(threadManager.io) {
      videoApi.getPlaybackToken()
    }
  }

  /**
   * Update content Playback
   */
  @Throws(Exception::class)
  suspend fun updateContentPlayback(
    playbackToken: String,
    contentId: String,
    progress: Long,
    seconds: Long
  ): Response<Content> {
    val playbackProgress = PlaybackProgress(playbackToken, progress, seconds)
    return withContext(threadManager.io) {
      videoApi.updateContentPlayback(contentId, playbackProgress)
    }
  }
}
