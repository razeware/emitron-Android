package com.raywenderlich.emitron.ui.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raywenderlich.emitron.data.settings.SettingsRepository
import com.raywenderlich.emitron.data.video.VideoRepository
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.ui.mytutorial.bookmarks.BookmarkActionDelegate
import com.raywenderlich.emitron.utils.Event
import com.raywenderlich.emitron.utils.Log
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * ViewModel for Player view
 */
class PlayerViewModel @Inject constructor(
  private val repository: VideoRepository,
  private val bookmarkActionDelegate: BookmarkActionDelegate,
  private val settingsRepository: SettingsRepository
) : ViewModel() {

  private val _nowPlayingEpisode = MutableLiveData<Data>()

  private var nowPlayingPosition: Int = 0

  private val _nextEpisode = MutableLiveData<Data>()

  private val _playlist = MutableLiveData<Playlist>()

  private val _playerToken = MutableLiveData<String>()

  private var lastUpdatedProgress: Long = 0

  private val _serverContentProgress = MutableLiveData<Long>()

  private val _resetPlaybackToken = MutableLiveData<Boolean>()

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
    episode.getVideoId()?.let { videoId ->
      viewModelScope.launch {
        try {
          val videoContent = repository.getVideoStream(videoId)
          val playerToken = repository.getVideoPlaybackToken().getPlayerToken()
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
  fun updateProgress(duration: Long) {
    val progress = duration - lastUpdatedProgress
    val playbackToken = _playerToken.value ?: ""
    val contentId = _nowPlayingEpisode.value?.id ?: return

    if (duration - lastUpdatedProgress > 5000) {
      lastUpdatedProgress = duration

      viewModelScope.launch {

        val result = try {
          val response = repository.updateContentPlayback(
            playbackToken,
            contentId,
            duration,
            progress
          )
          val serverProgress = response.body()?.getProgress() ?: 0
          if (serverProgress > lastUpdatedProgress) {
            _serverContentProgress.value = serverProgress
          }
          if (!response.isSuccessful) {
            _resetPlaybackToken.value = response.code() == 400
          }
          response.isSuccessful
        } catch (exception: IOException) {
          false
        } catch (exception: HttpException) {
          _resetPlaybackToken.value = exception.code() == 400
          false
        }

        if (!result) {
          Log.exception(IllegalArgumentException("Failed to update progress"))
        }
      }
    }
  }

  /**
   * Create a new playback token and resume playback
   */
  fun resumePlayback() {
    viewModelScope.launch {

      val resetPlaybackToken = try {
        _playerToken.value = repository.getVideoPlaybackToken().getPlayerToken()
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
}

