package com.raywenderlich.emitron.ui.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raywenderlich.emitron.data.progressions.ProgressionRepository
import com.raywenderlich.emitron.data.settings.SettingsRepository
import com.raywenderlich.emitron.data.video.VideoRepository
import com.raywenderlich.emitron.model.Content
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.ui.mytutorial.bookmarks.BookmarkActionDelegate
import com.raywenderlich.emitron.utils.Event
import com.raywenderlich.emitron.utils.Log
import com.raywenderlich.emitron.utils.Logger
import com.raywenderlich.emitron.utils.LoggerImpl
import com.raywenderlich.emitron.utils.extensions.isBadRequest
import kotlinx.coroutines.launch
import org.threeten.bp.Clock
import org.threeten.bp.LocalDateTime
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

/**
 * ViewModel for Player view
 */
class PlayerViewModel @Inject constructor(
  private val repository: VideoRepository,
  private val bookmarkActionDelegate: BookmarkActionDelegate,
  private val settingsRepository: SettingsRepository,
  private val progressionRepository: ProgressionRepository,
  private val logger: LoggerImpl
) : ViewModel(), Logger by logger {

  private val _nowPlayingEpisode = MutableLiveData<Data>()

  private var nowPlayingPosition: Int = 0

  private val _nextEpisode = MutableLiveData<Data>()

  private val _playlist = MutableLiveData<Playlist>()

  private val _playerToken = MutableLiveData<String>()

  private var lastUpdatedProgress: Long = 0

  private val _serverContentProgress = MutableLiveData<Long>()

  private val _resetPlaybackToken = MutableLiveData<Boolean>()

  private val _enqueueOfflineProgressUpdate = MutableLiveData<String>()

  /**
   * Observer for current episode
   *
   */
  val currentEpisode: LiveData<Data>
    get() = _nowPlayingEpisode

  /**
   * Observer for next episode
   *
   */
  val nextEpisode: LiveData<Data>
    get() = _nextEpisode

  /**
   * Observer for player token
   * If player token value changes, we should resume playback
   */
  val playerToken: LiveData<String>
    get() = _playerToken

  /**
   * Observer for bookmark action
   *
   */
  val bookmarkActionResult: LiveData<Event<BookmarkActionDelegate.BookmarkActionResult>> =
    bookmarkActionDelegate.bookmarkActionResult

  /**
   * Observer for server content progress
   */
  val serverContentProgress: LiveData<Long>
    get() = _serverContentProgress

  /**
   * Observer for server content progress
   */
  val resetPlaybackToken: LiveData<Boolean>
    get() = _resetPlaybackToken

  /**
   * Observer for playlist
   */
  val playlist: LiveData<Playlist>
    get() = _playlist

  /**
   * Observer for offline progress update
   */
  val enqueueOfflineProgressUpdate: LiveData<String>
    get() = _enqueueOfflineProgressUpdate

  /**
   * Start playback
   *
   * @param playlist
   */
  fun startPlayback(playlist: Playlist) {
    val episodes = playlist.episodes
    if (episodes.isEmpty()) {
      return
    }
    _playlist.value = playlist
    val currentEpisode = playlist.currentEpisode ?: episodes[0]
    nowPlayingPosition = episodes.indexOf(currentEpisode)
    initNextEpisode(episodes, nowPlayingPosition)
    startPlayback(currentEpisode, nowPlayingPosition)
  }

  private fun startPlayback(episode: Data, position: Int) {
    if (episode.isDownloaded()) {
      lastUpdatedProgress = 0
      _nowPlayingEpisode.value = episode
      nowPlayingPosition = position
    } else {
      startStream(episode, position)
    }
  }

  private fun startStream(episode: Data, position: Int) {
    episode.getVideoId()?.let { videoId ->
      viewModelScope.launch {
        try {
          val videoContent = repository.getVideoStream(videoId)
          val playerToken = repository.getVideoPlaybackToken()?.getPlayerToken()
          _playerToken.value = playerToken
          lastUpdatedProgress = 0
          _nowPlayingEpisode.value = episode.setVideoUrl(videoContent.datum)
          nowPlayingPosition = position
        } catch (error: Throwable) {
          Log.exception(error)
        }
      }
    }
  }

  private fun hasNextPlaybackPosition(
    playlist: List<Data>,
    currentEpisodePosition: Int
  ): Pair<Boolean, Int> {
    val playlistSize = playlist.size
    val nextPlayingPosition = currentEpisodePosition + 1
    return if ((nextPlayingPosition) < playlistSize) {
      true to nextPlayingPosition
    } else {
      false to 0
    }
  }

  /**
   * Play next episode
   */
  fun playNextEpisode() {
    val episodes = _playlist.value?.episodes
    if (!episodes.isNullOrEmpty()) {
      val (hasNext, nextEpisodePosition) =
        hasNextPlaybackPosition(episodes, nowPlayingPosition)
      if (hasNext) {
        val currentEpisode = episodes[nextEpisodePosition]
        startPlayback(currentEpisode, nextEpisodePosition)
      }
      initNextEpisode(episodes, nextEpisodePosition)
    }
  }

  private fun initNextEpisode(
    playlist: List<Data>,
    currentEpisodePosition: Int
  ) {
    val (hasNext, nextEpisodePosition) =
      hasNextPlaybackPosition(playlist, currentEpisodePosition)
    if (hasNext) {
      val nextEpisode = playlist[nextEpisodePosition]
      _nextEpisode.value = nextEpisode
    } else {
      _nextEpisode.value = null
    }
  }

  /**
   * Add or remove collection from bookmarks
   */
  fun updateContentBookmark() {
    val playingEpisode = _nowPlayingEpisode.value
    viewModelScope.launch {
      bookmarkActionDelegate.updateContentBookmark(playingEpisode)
    }
  }

  /**
   * Save playback speed
   *
   * @param speed New playback speed
   */
  fun updatePlaybackSpeed(speed: Float) {
    settingsRepository.updateSelectedPlaybackSpeed(speed)
  }

  /**
   * Save playback quality
   *
   * @param quality New playback quality
   */
  fun updatePlaybackQuality(quality: Int) {
    settingsRepository.updateSelectedPlaybackQuality(quality)
  }

  /**
   * Save playback subtitle language
   *
   * @param language New subtitle language
   */
  fun updateSubtitleLanguage(language: String) {
    settingsRepository.updateSubtitleLanguage(language)
  }

  /**
   * Save auto play next
   *
   * @param allowed true if next episode should be auto-played, else false
   */
  fun updateAutoPlayNext(allowed: Boolean) {
    settingsRepository.updateAutoPlaybackAllowed(allowed)
  }

  /**
   * Check next episode should be auto-played
   *
   * @return true if next episode should be auto-played, else false
   */
  fun shouldAutoPlay(): Boolean = settingsRepository.getAutoPlayNextAllowed()

  /**
   * Get saved playback subtitle language
   *
   * @return Playback subtitle language
   */
  fun getSubtitleLanguage(): String = settingsRepository.getSubtitleLanguage()

  /**
   * Get saved playback quality
   *
   * @return Playback quality
   */
  fun getPlaybackQuality(): Int = settingsRepository.getPlaybackQuality()

  /**
   * Get saved playback speed
   *
   * @return Playback speed
   */
  fun getPlaybackSpeed(): Float = settingsRepository.getPlaybackSpeed()

  /**
   * Get title for current episode
   *
   * @return title
   */
  fun getNowPlayingTitle(): String? {
    val playingEpisode = _nowPlayingEpisode.value

    return if (null != playingEpisode) {
      playingEpisode.getName()
    } else {
      ""
    }
  }

  /**
   * Get description for current episode
   *
   * @return description
   */
  fun getNowPlayingDescription(): String? {
    val playingEpisode = _nowPlayingEpisode.value

    return if (null != playingEpisode) {
      playingEpisode.getDescription()
    } else {
      ""
    }
  }

  /**
   * Get cover art url for current episode
   *
   * @return cover art url
   */
  fun getNowPlayingCoverArt(): String? {
    val playingEpisode = _nowPlayingEpisode.value

    return if (null != playingEpisode) {
      playingEpisode.getCardArtworkUrl()
    } else {
      ""
    }
  }

  /**
   * Update content progress to server
   */
  fun updateProgress(
    isConnected: Boolean,
    progressInMillis: Long,
    updatedAt: LocalDateTime = LocalDateTime.now(Clock.systemUTC())
  ) {
    val contentId = _nowPlayingEpisode.value?.id ?: return
    if (progressInMillis == 0L) {
      return
    }

    viewModelScope.launch {
      if (isConnected) {
        updateOnlineProgress(contentId, progressInMillis)
      } else {
        updateOfflineProgress(contentId, progressInMillis, updatedAt)
      }
    }
  }

  private fun getPlaybackProgress(progressInSecs: Long): Long? {
    val progress = progressInSecs - lastUpdatedProgress
    return if (progress >= PROGRESS_UPDATE_DURATION_INTERVAL) {
      lastUpdatedProgress = progressInSecs
      progress // converting millis to secs
    } else {
      null
    }
  }

  private suspend fun updateOnlineProgress(contentId: String, progressInMillis: Long) {
    val playbackToken = _playerToken.value ?: ""

    val progressInSecs = progressInMillis / MILLIS_IN_A_SEC
    val progress = getPlaybackProgress(progressInSecs) ?: return

    val result = try {
      val response = progressionRepository.updatePlaybackProgress(
        playbackToken,
        contentId,
        progressInSecs,
        progress
      )
      verifyRemoteProgressDiff(response)
    } catch (exception: IOException) {
      false
    } catch (exception: HttpException) {
      _resetPlaybackToken.value = exception.isBadRequest()
      false
    }

    if (!result) {
      log(IllegalArgumentException("Failed to update progress"))
    }
  }

  private fun verifyRemoteProgressDiff(response: Response<Content>): Boolean {
    val serverProgress = response.body()?.getProgress() ?: 0
    if (serverProgress > lastUpdatedProgress) {
      _serverContentProgress.value = serverProgress
    }
    if (!response.isSuccessful) {
      _resetPlaybackToken.value = response.isBadRequest()
    }
    return response.isSuccessful
  }

  private suspend fun updateOfflineProgress(
    contentId: String,
    progressInMillis: Long,
    updatedAt: LocalDateTime
  ) {
    val progressInSeconds = progressInMillis / MILLIS_IN_A_SEC
    val playbackDuration = _nowPlayingEpisode.value?.getDuration() ?: return

    val percentageCompletion = getPercentageCompletion(progressInSeconds, playbackDuration)
    val finished = percentageCompletion in COMPLETION_PERCENTAGE
    progressionRepository.updateLocalProgression(
      contentId,
      percentageCompletion,
      progressInSeconds,
      finished,
      false,
      updatedAt
    )
    _enqueueOfflineProgressUpdate.value = contentId
  }

  private fun getPercentageCompletion(progress: Long, duration: Long): Int {
    return ((progress.toDouble() / duration.toDouble()) * 100).toInt()
  }

  /**
   * Create a new playback token and resume playback
   */
  fun resumePlayback() {
    viewModelScope.launch {

      val resetPlaybackToken = try {
        _playerToken.value = repository.getVideoPlaybackToken()?.getPlayerToken()
        false
      } catch (error: Throwable) {
        Log.exception(error)
        true
      }

      _resetPlaybackToken.value = resetPlaybackToken
    }
  }

  /**
   * Is current playing content screencast or video course
   *
   * @return true if content is screencast, else false
   */
  fun isContentTypeScreencast(): Boolean {
    val content = _playlist.value?.collection
    return content?.isTypeScreencast() == true
  }

  /**
   * Check if there are more episodes after current episode
   *
   * @return true if there is next episode, else false
   */
  fun hasMoreEpisodes(): Boolean {
    val episodes = _playlist.value?.episodes
    return !episodes.isNullOrEmpty() && episodes.size - 1 > nowPlayingPosition
  }

  companion object {
    private const val PROGRESS_UPDATE_DURATION_INTERVAL: Long = 5
    private const val MILLIS_IN_A_SEC: Long = 1000L
    private val COMPLETION_PERCENTAGE: Array<Int> = arrayOf(99, 100)
  }

  /**
   * Get all playlist episodes
   */
  fun getAllEpisodes(): List<Data> = _playlist.value?.episodes ?: emptyList()
}

