package com.raywenderlich.emitron.data.video

import com.raywenderlich.emitron.data.content.ContentApi
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
  private val api: VideoApi,
  private val contentApi: ContentApi,
  private val threadManager: ThreadManager
) {

  /**
   * Get video stream
   */
  @Throws(Exception::class)
  suspend fun getVideoStream(id: String): Content {
    return withContext(threadManager.io) {
      api.getVideoStream(id)
    }
  }

  /**
   * Get video playback token
   */
  @Throws(Exception::class)
  suspend fun getVideoPlaybackToken(): Content {
    return withContext(threadManager.io) {
      contentApi.getPlaybackToken()
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
      contentApi.updateContentPlayback(contentId, playbackProgress)
    }
  }
}
