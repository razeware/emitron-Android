package com.raywenderlich.emitron.ui.player

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.support.v4.media.session.MediaSessionCompat
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.framework.CastContext
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.ui.download.DownloadService.Companion.buildCacheDataSourceFactory
import com.raywenderlich.emitron.ui.player.PlayerFragment.Companion.defaultPlaybackQuality
import com.raywenderlich.emitron.ui.player.cast.Episode
import com.raywenderlich.emitron.utils.extensions.toVisibility
import java.util.*

/**
 * Manages media player instance.
 */
class PlayerManager constructor(private val userAgent: String, lifecycle: Lifecycle) :
  Player.EventListener, SessionAvailabilityListener, LifecycleObserver {

  private val mediaQueue: ArrayList<Episode>
  private var mediaPlayer: SimpleExoPlayer? = null
  private lateinit var localPlayerView: PlayerView
  private lateinit var castControlView: PlayerControlView
  private val stateObserver = MutableLiveData<MediaPlaybackState>()
  private lateinit var eventObserver: Observer<MediaPlaybackState>
  private var castPlayer: CastPlayer? = null
  private var concatenatingMediaSource: ConcatenatingMediaSource? = null
  private lateinit var playerNotificationManager: PlayerNotificationManager
  private lateinit var mediaSessionCompat: MediaSessionCompat
  private lateinit var castControlGroup: View

  /**
   * Check if playback has started
   */
  var hasPlaybackStarted: Boolean = false
    private set

  private lateinit var trackSelector: DefaultTrackSelector

  private lateinit var dataSourceFactory: DefaultHttpDataSourceFactory

  private lateinit var cacheDataSourceFactory: CacheDataSourceFactory

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
    playbackControlView: PlayerView,
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
    this.trackSelector = buildTrackSelector()
    this.dataSourceFactory = buildDataSourceFactory(context, userAgent)
    this.cacheDataSourceFactory = buildCacheDataSourceFactory(cache, userAgent)
    stateObserver.observeForever(eventObserver)
    reinitialize(castContext)
  }

  private fun reinitialize(castContext: CastContext) {
    if (::localPlayerView.isInitialized) {
      if (mediaPlayer == null) {
        mediaPlayer = createMediaPlayer(localPlayerView, trackSelector)
      }

      if (castPlayer == null) {
        castPlayer = createCastMediaPlayer(castContext, castControlView, this, this)
      }

      val currentPlayer =
        if (castPlayer?.isCastSessionAvailable == true) castPlayer else mediaPlayer
      setCurrentPlayer(currentPlayer)

      val mediaSessionConnector = MediaSessionConnector(mediaSessionCompat)
      mediaSessionConnector.setPlayer(currentPlayer)
      with(playerNotificationManager) {
        setPlayer(currentPlayer)
        setUseNavigationActions(false)
        setFastForwardIncrementMs(0)
        setRewindIncrementMs(0)
        setUseStopAction(false)
        setUseChronometer(true)
        setUsePlayPauseActions(true)
        setSmallIcon(R.drawable.ic_logo)
        setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
        setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        setMediaSessionToken(mediaSessionCompat.sessionToken)
      }
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
        castPlayer?.addItems(buildMediaQueueItem(episode))
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

  // Player.EventListener implementation.

  /**
   * See [Player.EventListener.onPlayerStateChanged]
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
   * See [Player.EventListener.onPositionDiscontinuity]
   */
  override fun onPositionDiscontinuity(@Player.DiscontinuityReason reason: Int) {
    updateCurrentItemIndex()
  }

  /**
   * See [Player.EventListener.onTimelineChanged]
   */
  override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
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
   * See [Player.EventListener.onPlayerError]
   */
  override fun onPlayerError(error: ExoPlaybackException?) {
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
      localPlayerView.toVisibility(true)
      castControlGroup.toVisibility(false)
      castControlView.hide()
    } else
    /* currentPlayer == castPlayer */ {
      localPlayerView.toVisibility(false)
      castControlGroup.toVisibility(true)
      castControlView.show()
    }

    this.currentPlayer = currentPlayer
    updateMediaQueue(false)
  }

  private fun updateMediaQueue(overridePlayWhenReady: Boolean) {
    // Player state management.
    var playbackPositionMs = C.TIME_UNSET
    var windowIndex = C.INDEX_UNSET
    var playWhenReady = false
    if (null != this.currentPlayer) {
      val playbackState = this.currentPlayer?.playbackState
      if (playbackState != Player.STATE_ENDED) {
        playbackPositionMs = this.currentPlayer?.currentPosition ?: C.TIME_UNSET
        playWhenReady = this.currentPlayer?.playWhenReady ?: true
        windowIndex = this.currentPlayer?.currentWindowIndex ?: C.INDEX_UNSET
        if (windowIndex != currentItemIndex) {
          playbackPositionMs = C.TIME_UNSET
          windowIndex = currentItemIndex
        }
      }
      this.currentPlayer?.stop(true)
    }

    if (currentPlayer == mediaPlayer) {
      concatenatingMediaSource = ConcatenatingMediaSource()
      for (i in mediaQueue.indices) {
        concatenatingMediaSource?.addMediaSource(
          buildMediaSource(
            mediaQueue[i],
            dataSourceFactory,
            cacheDataSourceFactory
          )
        )
      }
      mediaPlayer?.prepare(concatenatingMediaSource)
      mediaPlayer?.addListener(this@PlayerManager)
      if (playbackPositionMs != C.TIME_UNSET) {
        mediaPlayer?.seekTo(playbackPositionMs)
        mediaPlayer?.playWhenReady = overridePlayWhenReady || playWhenReady
      } else {
        mediaPlayer?.playWhenReady = overridePlayWhenReady
      }
    } else {
      val items = arrayOfNulls<MediaQueueItem>(mediaQueue.size)
      for (i in items.indices) {
        items[i] = buildMediaQueueItem(mediaQueue[i])
      }
      if (items.isNotEmpty() && currentItemIndex != C.INDEX_UNSET) {
        castPlayer?.loadItems(items, windowIndex, playbackPositionMs, Player.REPEAT_MODE_OFF)
      }
    }
  }

  /**
   * Start playback
   */
  fun play(shouldAutoPlay: Boolean) {
    warnLowVolume()
    if (hasPlaybackEnded()) {
      replay()
    }
    updateMediaQueue(shouldAutoPlay)
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
   * Enable/Disable subtitles
   *
   * @param subtitleLanguage true if subtitles enabled, else false
   */
  fun updateSubtitles(subtitleLanguage: String) {
    val mappedTrackInfo = trackSelector.currentMappedTrackInfo
    if (null != mappedTrackInfo) {
      val builder = trackSelector.buildUponParameters()
      for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
        if (mappedTrackInfo.getRendererType(rendererIndex) == C.TRACK_TYPE_TEXT) {
          if (subtitleLanguage.isEmpty()) {
            builder.clearSelectionOverrides(rendererIndex)
          } else {
            val subtitleOverride =
              getPlaybackSubtitleOverride(rendererIndex, mappedTrackInfo, subtitleLanguage)
            if (null != subtitleOverride) {
              builder.setSelectionOverride(
                rendererIndex,
                mappedTrackInfo.getTrackGroups(rendererIndex),
                subtitleOverride
              )
            }
          }
        }
      }
      trackSelector.setParameters(builder)
    }
  }

  /**
   * Update playback quality
   *
   * @param quality [PlayerFragment.playerPlaybackQualityOptions]
   */
  fun updatePlaybackQuality(quality: Int) {
    val mappedTrackInfo = trackSelector.currentMappedTrackInfo
    if (null != mappedTrackInfo) {
      val builder = trackSelector.buildUponParameters()
      for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
        if (mappedTrackInfo.getRendererType(rendererIndex) == C.TRACK_TYPE_VIDEO) {
          if (quality == defaultPlaybackQuality) {
            builder.clearSelectionOverrides(rendererIndex)
          } else {
            val qualityOverride =
              getPlaybackQualityOverride(rendererIndex, mappedTrackInfo, quality)
            if (null != qualityOverride) {
              builder.setSelectionOverride(
                rendererIndex,
                mappedTrackInfo.getTrackGroups(rendererIndex),
                qualityOverride
              )
            }
          }
        }
      }
      trackSelector.setParameters(builder)
    }
  }

  /**
   * Update default playback settings
   *
   * @param activity Context
   * @param speed [PlayerFragment.playerPlaybackSpeedOptions]
   * @param quality [PlayerFragment.playerPlaybackQualityOptions]
   * @param subtitleLanguage [PlayerFragment.playerSubtitleLanguageOptions]
   */
  fun setDefaultSettings(
    activity: Activity,
    speed: Float,
    quality: Int,
    subtitleLanguage: String
  ) {

    val builder = trackSelector.buildUponParameters()
    builder.setViewportSizeToPhysicalDisplaySize(activity, false)

    val mappedTrackInfo = trackSelector.currentMappedTrackInfo
    if (null != mappedTrackInfo) {

      for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
        builder.clearSelectionOverrides(rendererIndex)
        if (mappedTrackInfo.getRendererType(rendererIndex) == C.TRACK_TYPE_VIDEO) {
          val qualityOverride = getPlaybackQualityOverride(rendererIndex, mappedTrackInfo, quality)
          if (null != qualityOverride) {
            builder.setSelectionOverride(
              rendererIndex,
              mappedTrackInfo.getTrackGroups(rendererIndex),
              qualityOverride
            )
          }
        }
        if (mappedTrackInfo.getRendererType(rendererIndex) == C.TRACK_TYPE_TEXT) {
          val subtitleOverride =
            getPlaybackSubtitleOverride(rendererIndex, mappedTrackInfo, subtitleLanguage)
          if (null != subtitleOverride) {
            builder.setSelectionOverride(
              rendererIndex,
              mappedTrackInfo.getTrackGroups(rendererIndex),
              subtitleOverride
            )
          }
        }
      }
    }

    trackSelector.setParameters(builder)

    val playbackParameters = PlaybackParameters(speed)
    mediaPlayer?.playbackParameters = playbackParameters
  }

  private fun getPlaybackQualityOverride(
    rendererIndex: Int,
    mappedTrackInfo: MappingTrackSelector.MappedTrackInfo,
    quality: Int
  ): DefaultTrackSelector.SelectionOverride? {
    val trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex)
    for (groupIndex in 0 until trackGroups.length) {
      val group = trackGroups.get(groupIndex)
      for (trackIndex in 0 until group.length) {
        if (group.getFormat(trackIndex).height == quality) {
          if (mappedTrackInfo.getTrackSupport(
              rendererIndex,
              groupIndex,
              trackIndex
            ) == RendererCapabilities.FORMAT_HANDLED
          ) {
            return DefaultTrackSelector.SelectionOverride(groupIndex, trackIndex)
          }
        }
      }
    }
    return null
  }

  private fun getPlaybackSubtitleOverride(
    rendererIndex: Int,
    mappedTrackInfo: MappingTrackSelector.MappedTrackInfo,
    language: String
  ): DefaultTrackSelector.SelectionOverride? {
    val trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex)
    for (groupIndex in 0 until trackGroups.length) {
      val group = trackGroups.get(groupIndex)
      for (trackIndex in 0 until group.length) {
        if (group.getFormat(trackIndex).language.equals(language, true)) {
          if (mappedTrackInfo.getTrackSupport(
              rendererIndex,
              groupIndex,
              trackIndex
            ) == RendererCapabilities.FORMAT_HANDLED
          ) {
            return DefaultTrackSelector.SelectionOverride(groupIndex, trackIndex)
          }
        }
      }
    }
    return null
  }

  /**
   * Update playback speed
   */
  fun updatePlaybackSpeed(speed: Float) {
    val playbackParameters = PlaybackParameters(speed)
    mediaPlayer?.playbackParameters = playbackParameters
  }

  companion object {


    internal fun createMediaPlayer(
      playerView: PlayerView,
      trackSelector: DefaultTrackSelector
    ): SimpleExoPlayer {
      val mediaPlayer = ExoPlayerFactory.newSimpleInstance(playerView.context, trackSelector)
      playerView.player = mediaPlayer
      return mediaPlayer
    }

    internal fun createCastMediaPlayer(
      castCtx: CastContext,
      castControlView: PlayerControlView,
      castPlayerEventListener: Player.EventListener,
      sessionListener: SessionAvailabilityListener
    ): CastPlayer {
      val castPlayer = CastPlayer(castCtx)
      castPlayer.addListener(castPlayerEventListener)
      castPlayer.setSessionAvailabilityListener(sessionListener)
      castControlView.player = castPlayer
      return castPlayer
    }

    internal fun buildMediaSource(
      episode: Episode,
      dataSourceFactory: DefaultHttpDataSourceFactory,
      cacheDataSourceFactory: CacheDataSourceFactory
    ): MediaSource {
      val uri = Uri.parse(episode.uri)
      when (episode.mimeType) {
        MimeTypes.APPLICATION_M3U8 -> return HlsMediaSource.Factory(dataSourceFactory)
          .setAllowChunklessPreparation(true)
          .createMediaSource(uri)
        MimeTypes.APPLICATION_MP4 -> return ProgressiveMediaSource
          .Factory(cacheDataSourceFactory)
          .createMediaSource(uri)
        else -> {
          throw IllegalStateException("Unsupported type: " + episode.mimeType)
        }
      }
    }

    internal fun buildMediaQueueItem(episode: Episode): MediaQueueItem {
      val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
      movieMetadata.putString(MediaMetadata.KEY_TITLE, episode.name)
      val mediaInfo = MediaInfo.Builder(episode.uri)
        .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED).setContentType(episode.mimeType)
        .setMetadata(movieMetadata).build()
      return MediaQueueItem.Builder(mediaInfo).build()
    }

    internal fun buildTrackSelector(): DefaultTrackSelector {
      val trackSelectionFactory = AdaptiveTrackSelection.Factory()
      return DefaultTrackSelector(trackSelectionFactory).apply {
        parameters = DefaultTrackSelector.ParametersBuilder().build()
      }
    }

    internal fun buildDataSourceFactory(
      context: Context,
      userAgent: String
    ): DefaultHttpDataSourceFactory {
      val bandWidthMeter =
        DefaultBandwidthMeter.Builder(context).build()
      return DefaultHttpDataSourceFactory(userAgent, bandWidthMeter)
    }
  }
}
