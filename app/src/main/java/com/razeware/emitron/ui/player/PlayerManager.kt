package com.razeware.emitron.ui.player

import android.content.Context
import android.media.AudioManager
import android.support.v4.media.session.MediaSessionCompat
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.StyledPlayerControlView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.gms.cast.framework.CastContext
import com.razeware.emitron.R
import com.razeware.emitron.ui.download.DownloadService.Companion.buildCacheDataSourceFactory
import com.razeware.emitron.ui.player.PlayerManagerFactory.buildDataSourceFactory
import com.razeware.emitron.ui.player.PlayerManagerFactory.buildMediaItem
import com.razeware.emitron.ui.player.PlayerManagerFactory.buildMediaSource
import com.razeware.emitron.ui.player.PlayerManagerFactory.buildTrackSelector
import com.razeware.emitron.ui.player.PlayerManagerFactory.createCastMediaPlayer
import com.razeware.emitron.ui.player.PlayerManagerFactory.createMediaPlayer
import com.razeware.emitron.ui.player.cast.Episode

/**
 * Manages media player instance.
 */
class PlayerManager constructor(private val userAgent: String, lifecycle: Lifecycle) :
  Player.Listener, SessionAvailabilityListener, LifecycleObserver {

  private val mediaQueue: ArrayList<Episode>
  private var mediaPlayer: ExoPlayer? = null
  private lateinit var localPlayerView: StyledPlayerControlView
  private lateinit var castControlView: PlayerControlView
  private val stateObserver = MutableLiveData<MediaPlaybackState>()
  private lateinit var eventObserver: Observer<MediaPlaybackState>
  private var castPlayer: CastPlayer? = null
  private var concatenatingMediaSource: ConcatenatingMediaSource? = null
  private lateinit var playerNotificationManager: PlayerNotificationManager
  private lateinit var mediaSessionCompat: MediaSessionCompat
  private lateinit var castControlGroup: View
  private lateinit var playerConfigManager: PlayerConfigManager

  /**
   * Check if playback has started
   */
  var hasPlaybackStarted: Boolean = false
    private set

  private lateinit var trackSelector: DefaultTrackSelector

  private lateinit var dataSourceFactory: DefaultHttpDataSource.Factory

  private lateinit var cacheDataSourceFactory: CacheDataSource.Factory

  /**
   * Returns the index of the currently played item.
   */
  private var currentItemIndex: Int = 0

  private var currentPlayer: Player? = null

  init {
    lifecycle.addObserver(this)
    mediaQueue = ArrayList()
    currentItemIndex = C.INDEX_UNSET
  }

  /**
   * Init player manager
   */
  fun initialise(
    context: Context,
    castContext: CastContext,
    playerNotificationManager: PlayerNotificationManager,
    playbackControlView: StyledPlayerControlView,
    castControlView: PlayerControlView,
    castControlGroup: View,
    eventObserver: Observer<MediaPlaybackState>,
    cache: Cache
  ) {
    this.localPlayerView = playbackControlView
    this.castControlView = castControlView
    this.eventObserver = eventObserver
    this.playerNotificationManager = playerNotificationManager
    this.mediaSessionCompat = MediaSessionCompat(context.applicationContext, context.packageName)
    this.castControlGroup = castControlGroup
    this.trackSelector = buildTrackSelector(context)
    this.dataSourceFactory = buildDataSourceFactory(context, userAgent)
    this.cacheDataSourceFactory = buildCacheDataSourceFactory(cache, userAgent)
    stateObserver.observeForever(eventObserver)
    reinitialize(castContext)
  }

  private fun reinitialize(castContext: CastContext) {
    if (::localPlayerView.isInitialized) {
      initMediaPlayer()
      initCastPlayer(castContext)
      initCurrentPlayer()
      MediaSessionConnector(mediaSessionCompat).setPlayer(currentPlayer)
      initPlayerNotificationManager()
    }

    this.playerConfigManager = PlayerConfigManager(trackSelector, mediaPlayer)
  }

  private fun initMediaPlayer() {
    if (mediaPlayer == null) {
      mediaPlayer = createMediaPlayer(localPlayerView, trackSelector)
    }
  }

  private fun initCastPlayer(castContext: CastContext) {
    if (castPlayer == null) {
      castPlayer = createCastMediaPlayer(castContext, castControlView, this, this)
    }
  }

  private fun initCurrentPlayer() {
    val currentPlayer =
      if (castPlayer?.isCastSessionAvailable == true) castPlayer else mediaPlayer
    setCurrentPlayer(currentPlayer)
  }

  private fun initPlayerNotificationManager() {
    with(playerNotificationManager) {
      setPlayer(currentPlayer)
      setUseNextAction(false)
      setUsePreviousAction(false)
      setUseStopAction(false)
      setUseChronometer(true)
      setUsePlayPauseActions(true)
      setSmallIcon(R.drawable.ic_logo)
      setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
      setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
      setMediaSessionToken(mediaSessionCompat.sessionToken)
    }
  }

  /**
   * Appends `episode` to the media queue.
   *
   * @param episode The [Episode] to append.
   */
  fun addItem(episode: Episode?) {
    mediaQueue.clear()
    episode?.let {
      mediaQueue.add(episode)
      if (currentPlayer == mediaPlayer) {
        concatenatingMediaSource?.addMediaSource(
          buildMediaSource(episode, dataSourceFactory, cacheDataSourceFactory)
        )
      } else {
        castPlayer?.addMediaItem(buildMediaItem(episode))
      }
    }
  }

  /**
   * Releases the manager and the players that it holds.
   */
  fun release() {
    currentItemIndex = C.INDEX_UNSET
    mediaQueue.clear()
    playerNotificationManager.setPlayer(null)
    mediaSessionCompat.release()
    castPlayer?.apply {
      setSessionAvailabilityListener(null)
      release()
    }
    castPlayer = null
    localPlayerView.player = null

    mediaPlayer?.apply {
      removeListener(this@PlayerManager)
      release()
    }
    mediaPlayer = null
    removeObserver()
  }

  // Player.Listener implementation.

  /**
   * See [Player.Listener.onPlayerStateChanged]
   */
  override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
    updateCurrentItemIndex()
    when (playbackState) {
      Player.STATE_BUFFERING -> stateObserver.value = MediaPlaybackState.BUFFERING
      Player.STATE_READY -> {
        hasPlaybackStarted = true
        stateObserver.value = MediaPlaybackState.READY
      }
      Player.STATE_ENDED -> stateObserver.value = MediaPlaybackState.COMPLETED
      Player.STATE_IDLE -> stateObserver.value = MediaPlaybackState.IDLE
    }
  }

  /**
   * See [Player.Listener.onPositionDiscontinuity]
   */
  override fun onPositionDiscontinuity(@Player.DiscontinuityReason reason: Int) {
    updateCurrentItemIndex()
  }

  /**
   * See [Player.Listener.onTimelineChanged]
   */
  override fun onTimelineChanged(timeline: Timeline, reason: Int) {
    updateCurrentItemIndex()
  }

  private fun updateCurrentItemIndex() {
    val playbackState = currentPlayer?.playbackState
    val currentItemIndex =
      if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
        currentPlayer?.currentWindowIndex ?: C.INDEX_UNSET
      } else {
        C.INDEX_UNSET
      }
    maybeSetCurrentItemAndNotify(currentItemIndex)
  }

  private fun maybeSetCurrentItemAndNotify(currentItemIndex: Int) {
    if (this.currentItemIndex != currentItemIndex) {
      this.currentItemIndex = currentItemIndex
    }
  }

  /**
   * See [Player.Listener.onPlayerError]
   */
  override fun onPlayerError(error: PlaybackException) {
    stateObserver.value = MediaPlaybackState.ERROR
  }

  // CastPlayer.SessionAvailabilityListener implementation.

  /**
   * See [SessionAvailabilityListener.onCastSessionAvailable]
   */
  override fun onCastSessionAvailable() {
    setCurrentPlayer(castPlayer)
  }

  /**
   * See [SessionAvailabilityListener.onCastSessionUnavailable]
   */
  override fun onCastSessionUnavailable() {
    setCurrentPlayer(mediaPlayer)
  }

  private fun setCurrentPlayer(currentPlayer: Player?) {
    if (currentPlayer == null) {
      return
    }

    if (this.currentPlayer === currentPlayer) {
      return
    }

    // View management.
    if (currentPlayer == mediaPlayer) {
      localPlayerView.visibility = View.VISIBLE
      castControlGroup.isVisible = false
      castControlView.hide()
    } else
    /* currentPlayer == castPlayer */ {
      localPlayerView.visibility = View.GONE
      castControlGroup.isVisible = true
      castControlView.show()
    }


    updateMediaQueue {
      this.currentPlayer = currentPlayer
    }
  }

  private fun updateMediaQueue(
    playerConfig: PlayerConfig = PlayerConfig(),
    onMediaQueueUpdate: (() -> Unit)? = null
  ) {

    // Get current player state
    val lastPlayerState = getCurrentPlayerState()

    // Change current player
    onMediaQueueUpdate?.invoke()

    val updatedPlayerState = if (playerConfig.reset) {
      lastPlayerState.copy(
        playbackPositionMs = playerConfig.duration,
        playWhenReady = playerConfig.overridePlayWhenReady
      )
    } else {
      lastPlayerState
    }

    // Update current player to last player state
    if (currentPlayer == mediaPlayer) {
      updateMediaPlayer(playerConfig, updatedPlayerState)
    } else {
      updateCastPlayer(updatedPlayerState)
    }
  }

  private fun getCurrentPlayerState(): PlayerState {
    val playerState = PlayerState.from(this.currentPlayer, currentItemIndex)
    this.currentPlayer?.stop(true)
    return playerState
  }

  /**
   * Update media player state
   *
   * @param playerConfig Current player config
   * @param playerState Current player state
   */
  private fun updateMediaPlayer(
    playerConfig: PlayerConfig,
    playerState: PlayerState
  ) {
    updateMediaSource()
    // concatenatingMediaSource?.getMediaSource(0)?.let { mediaPlayer?.setMediaSource(it) }
    for (episode in mediaQueue) {
      mediaPlayer?.setMediaSource(
        buildMediaSource(
          episode,
          dataSourceFactory,
          cacheDataSourceFactory
        )
      )
    }

    mediaPlayer?.addListener(this@PlayerManager)
    if (playerState.playbackPositionMs != C.TIME_UNSET) {
      if (!playerConfig.reset) {
        mediaPlayer?.seekTo(playerState.playbackPositionMs)
        mediaPlayer?.playWhenReady = playerConfig.overridePlayWhenReady || playerState.playWhenReady
      } else {
        mediaPlayer?.seekTo(playerConfig.duration)
        mediaPlayer?.playWhenReady = playerConfig.overridePlayWhenReady
      }
    }
  }

  /**
   * Update media source
   */
  private fun updateMediaSource() {
    concatenatingMediaSource = ConcatenatingMediaSource()
    for (episode in mediaQueue) {
      concatenatingMediaSource?.addMediaSource(
        buildMediaSource(
          episode,
          dataSourceFactory,
          cacheDataSourceFactory
        )
      )
    }
  }

  /**
   * Update current player to cast player
   *
   * @param playerState Current player state
   */
  private fun updateCastPlayer(playerState: PlayerState) {
    val items = mediaQueue.map {
      buildMediaItem(it)
    }.toTypedArray()
    if (items.isNotEmpty() && playerState.windowIndex != C.INDEX_UNSET) {
      // TODO Check how to set repeat mode off
      castPlayer?.setMediaItems(
        items.toMutableList(), playerState.windowIndex,
        playerState.playbackPositionMs
      )
      castPlayer?.playWhenReady = playerState.playWhenReady
    }
  }

  /**
   * Start playback
   */
  fun play(shouldAutoPlay: Boolean, seekTo: Long) {
    warnLowVolume()
    if (hasPlaybackEnded()) {
      replay()
    }

    val playerConfig = PlayerConfig(shouldAutoPlay, seekTo, true)
    updateMediaQueue(playerConfig)
  }

  private fun warnLowVolume() {
    if (null != localPlayerView.context) {
      val audio =
        localPlayerView.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
      val currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
      if (currentVolume < 2) {
        stateObserver.value = MediaPlaybackState.LOW_VOLUME
      }
    }
  }

  private fun replay() {
    mediaPlayer?.seekTo(0)
  }

  private fun hasPlaybackEnded() = mediaPlayer?.playbackState == Player.STATE_ENDED

  private fun removeObserver() {
    if (::eventObserver.isInitialized) {
      stateObserver.removeObserver(eventObserver)
    }
  }

  /**
   * Get current content position
   *
   * @return content position ms
   */
  fun getContentPosition(): Long {
    val player = currentPlayer ?: return 0L
    val playbackState = player.playbackState
    if (playbackState == Player.STATE_READY && player.playWhenReady) {
      return player.currentPosition
    }

    if (playbackState == Player.STATE_ENDED) {
      return player.currentPosition
    }

    return 0L
  }

  /**
   * Seek playback to position
   *
   * @param positionMs Seek position
   */
  fun seekTo(positionMs: Long) {
    currentPlayer?.seekTo(positionMs)
  }

  /**
   * Pause playback
   */
  fun pause() {
    currentPlayer?.playWhenReady = false
  }

  /**
   * Resume playback
   */
  fun resume() {
    currentPlayer?.playWhenReady = true
  }

  /**
   * Return player configuration manager
   */
  fun getPlayerConfigManager(): PlayerConfigManager = playerConfigManager
}
