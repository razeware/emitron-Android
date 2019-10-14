package com.raywenderlich.emitron.ui.player

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.gms.cast.framework.CastContext
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.raywenderlich.emitron.BuildConfig
import com.raywenderlich.emitron.MainViewModel
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.databinding.FragmentPlayerBinding
import com.raywenderlich.emitron.di.modules.viewmodel.ViewModelFactory
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.notifications.NotificationChannels
import com.raywenderlich.emitron.ui.common.getDefaultAppBarConfiguration
import com.raywenderlich.emitron.ui.mytutorial.bookmarks.BookmarkActionDelegate
import com.raywenderlich.emitron.ui.player.cast.Episode
import com.raywenderlich.emitron.utils.Log
import com.raywenderlich.emitron.utils.createCountDownTimer
import com.raywenderlich.emitron.utils.createMainThreadScheduledHandler
import com.raywenderlich.emitron.utils.extensions.*
import dagger.android.support.DaggerFragment
import javax.inject.Inject


/**
 * Player view
 */
class PlayerFragment : DaggerFragment() {

  /**
   * Playback constants
   */
  object Playback {

    /**
     * Supported playback speed
     */
    object Speed {
      /**
       * 0.5 times of normal speed
       */
      const val HALF: Float = 0.5f
      /**
       * 0.75 times of normal speed
       */
      const val NORMALx0_75: Float = 0.5f
      /**
       * Normal playback speed
       */
      const val NORMAL: Float = 1.0f
      /**
       * 1.25 times of normal speed
       */
      const val NORMALx1_25: Float = 1.25f
      /**
       * 1.5 times of normal speed
       */
      const val NORMALx1_50: Float = 1.75f
      /**
       * Double of normal speed
       */
      const val DOUBLE: Float = 2.0f
    }

    /**
     * Supported playback quality
     */
    object Quality {
      /**
       * Full HD
       */
      const val FHD: Int = 1080
      /**
       * HD
       */
      const val HD: Int = 720

      /**
       * quarter of a Full HD
       */
      const val QHD: Int = 540
      /**
       * one ninth of a Full HD
       */
      const val NHD: Int = 360
      /**
       * quarter of VGA
       */
      const val QVGA: Int = 240
      /**
       * AUTO
       */
      const val AUTO: Int = 1
    }

    /**
     * Supported subtitle language
     */
    object Subtitle {

      /**
       * English
       */
      const val ENGLISH: String = "en"
    }
  }

  companion object {

    /**
     * Subtitle english ISO format
     */
    const val subtitleLanguageEnglish: String = Playback.Subtitle.ENGLISH

    /**
     * Default playback quality (Auto)
     */
    const val defaultPlaybackQuality: Int = Playback.Quality.AUTO // Auto

    /**
     * Playback speed options in order shown in view
     */
    val playerPlaybackSpeedOptions: List<Float> =
      listOf(
        Playback.Speed.HALF,
        Playback.Speed.NORMALx0_75,
        Playback.Speed.NORMAL,
        Playback.Speed.NORMALx1_25,
        Playback.Speed.NORMALx1_50,
        Playback.Speed.DOUBLE
      )

    /**
     * Playback quality options in order shown in view
     */
    val playerPlaybackQualityOptions: List<Int> =
      listOf(
        Playback.Quality.AUTO,
        Playback.Quality.QVGA,
        Playback.Quality.NHD,
        Playback.Quality.QHD,
        Playback.Quality.HD,
        Playback.Quality.FHD
      )

    /**
     * Playback subtitle language options in order shown in view
     */
    val playerSubtitleLanguageOptions: List<String> =
      listOf("", subtitleLanguageEnglish)

    /**
     * No. of millis after which the auto-playback UI will update progress
     */
    const val AUTO_PLAYBACK_COUNTDOWN_INTERVAL: Long = 1000

    /**
     * No. of millis for which the auto-playback view will be shown
     */
    const val AUTO_PLAYBACK_COUNTDOWN_DURATION: Long = 5000

    /**
     * Playback Notification Id
     */
    const val PLAYBACK_NOTIFICATION_ID: Int = 8287
  }


  /**
   * Custom factory for viewmodel
   *
   * Custom factory provides app related dependencies
   */
  @Inject
  lateinit var viewModelFactory: ViewModelFactory

  /**
   * Cache for offline playback
   *
   */
  @Inject
  lateinit var cache: Cache

  private val viewModel: PlayerViewModel by viewModels { viewModelFactory }

  private val parentViewModel: MainViewModel by activityViewModels { viewModelFactory }

  private val args by navArgs<PlayerFragmentArgs>()

  private var isInPictureInPictureMode = false

  private val countDownTimer: CountDownTimer =
    createCountDownTimer(
      AUTO_PLAYBACK_COUNTDOWN_DURATION,
      AUTO_PLAYBACK_COUNTDOWN_INTERVAL,
      { timeInterval ->
        updateAutoPlaybackProgress(timeInterval)
      }, {
        binding.groupAutoPlayProgress.visibility = View.GONE
        viewModel.playNextEpisode()
      })

  private val playbackNotificationManager: PlayerNotificationManager by lazy {
    PlayerNotificationManager(
      requireActivity(),
      NotificationChannels.channelIdPlayback,
      PLAYBACK_NOTIFICATION_ID,
      NotificationDescriptionAdapter(requireActivity(), viewModel),
      null,
      PlayerNotificationActionAdapter(viewModel)
    )
  }

  private val playerManager by lazy(LazyThreadSafetyMode.NONE) {
    PlayerManager(
      BuildConfig.APPLICATION_ID,
      lifecycle = viewLifecycleOwner.lifecycle
    )
  }

  private lateinit var binding: FragmentPlayerBinding

  private lateinit var playerNextButton: MaterialButton

  private lateinit var castNextButton: MaterialButton

  private lateinit var playerBookmarkButton: MaterialButton

  private lateinit var settingsBottomSheet: BottomSheetDialog

  private lateinit var playbackTokenErrorBottomSheet: BottomSheetDialog

  private lateinit var subtitlesBottomSheet: BottomSheetDialog

  private lateinit var playbackBufferingProgress: ProgressBar

  private var progressHandler: Handler? = null

  private lateinit var castContext: CastContext

  private val pipActionDelegate: PipActionDelegate by lazy {
    PipActionDelegate(requireActivity())
  }

  /**
   * See [androidx.fragment.app.Fragment.onCreate]
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    castContext = CastContext.getSharedInstance(requireActivity())
  }

  /**
   * See [androidx.fragment.app.Fragment.onCreateView]
   */
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = setDataBindingView(R.layout.fragment_player, container)
    binding.data = viewModel.currentEpisode
    return binding.root
  }

  /**
   * See [androidx.fragment.app.Fragment.onViewCreated]
   */
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    requestLandscapeOrientation()
    requestFullScreen()
    initUi()
    initObservers()
    startPlayback()
  }

  private fun initToolbar() {
    with(binding.toolbar) {
      setupWithNavController(
        findNavController(),
        getDefaultAppBarConfiguration()
      )
      navigationIcon =
        VectorDrawableCompat.create(
          resources,
          R.drawable.ic_material_icon_close,
          null
        )
      (requireActivity() as AppCompatActivity).setSupportActionBar(this)
      (requireActivity() as AppCompatActivity).title = ""
    }
  }

  private fun initUi() {
    initToolbar()
    with(binding.playerView) {
      val buttonPlayerSubtitles: MaterialButton =
        findViewById(R.id.button_player_subtitles)
      buttonPlayerSubtitles.setOnClickListener {
        showSubtitleBottomSheet()
      }

      val playerSettings: MaterialButton =
        findViewById(R.id.button_player_settings)
      playerSettings.setOnClickListener {
        showPlayerSettingsBottomSheet()
      }

      playerBookmarkButton = findViewById(R.id.button_player_bookmark)
      playerBookmarkButton.setOnClickListener { viewModel.updateContentBookmark() }

      playbackBufferingProgress =
        findViewById(R.id.player_play_back_buffering)

      val buttonPlaylist: View = findViewById(R.id.button_player_playlist)
      buttonPlaylist.setOnClickListener {
        findNavController().popBackStack()
      }

      playerNextButton = findViewById(R.id.player_next_episode)
      playerNextButton.setOnClickListener { viewModel.playNextEpisode() }

      binding.playerView.setControllerVisibilityListener {
        binding.toolbar.toVisibility(it == View.VISIBLE)
      }
    }

    binding.buttonAutoPlayCancel.setOnClickListener {
      findNavController().popBackStack()
    }

    with(binding.castControlView) {
      castNextButton = findViewById(R.id.player_next_episode)
      castNextButton.setOnClickListener { viewModel.playNextEpisode() }
    }

    playerManager.initialise(
      requireContext(),
      castContext,
      playbackNotificationManager,
      binding.playerView,
      binding.castControlView,
      binding.castControlGroup,
      eventObserver =
      Observer {
        onPlaybackStateChange(it)
      },
      cache = cache
    )
  }

  private fun startPlayback() {
    val playlist = args.playlist
    if (playlist.isNotDownloaded() && isNetNotConnected()) {
      showErrorSnackbar(getString(R.string.error_no_connection))
      return
    }

    playlist?.let {
      viewModel.startPlayback(playlist)
    }
  }

  private fun updateAutoPlaybackProgress(timeInterval: Long) {
    binding.playerAutoPlayMessage.text = getString(
      R.string.progress_next_episode,
      ((timeInterval / 1000) + 1).toString()
    )
    binding.playerAutoPlayProgress.progress = (120 - (timeInterval / 50)).toInt()
  }


  private fun onPlaybackStateChange(mediaPlayerState: MediaPlaybackState?) {
    when (mediaPlayerState) {
      MediaPlaybackState.COMPLETED -> {
        handlePlaybackCompleted()
        parentViewModel.updateIsPlaying(false)
      }
      MediaPlaybackState.IDLE -> {
        parentViewModel.updateIsPlaying(false)
      }
      MediaPlaybackState.BUFFERING -> {
        playbackBufferingProgress.visibility = View.VISIBLE
      }
      MediaPlaybackState.READY -> {
        handlePlaybackReady()
      }
      MediaPlaybackState.LOW_VOLUME -> {
        Toast.makeText(requireActivity(), getString(R.string.player_volume_low), Toast.LENGTH_SHORT)
          .show()
      }
      MediaPlaybackState.ERROR, null ->
        Log.exception(IllegalArgumentException("Error occurred"))
      MediaPlaybackState.UNKNOWN_MEDIA ->
        Log.exception(IllegalArgumentException("Unknown media"))
    }
  }

  private fun handlePlaybackReady() {
    playbackBufferingProgress.visibility = View.GONE
    if (progressHandler == null)
      progressHandler = createMainThreadScheduledHandler(requireActivity(), 5000) {
        updateProgress()
      }
    parentViewModel.updateIsPlaying(true)
    playerManager.setDefaultSettings(
      requireActivity(),
      speed = viewModel.getPlaybackSpeed(),
      quality = viewModel.getPlaybackQuality(),
      subtitleLanguage = viewModel.getSubtitleLanguage()
    )
  }

  private fun handlePlaybackCompleted() {
    val shouldShowAutoPlayback =
      viewModel.shouldAutoPlay() &&
          playerManager.hasPlaybackStarted &&
          viewModel.hasMoreEpisodes()

    if (shouldShowAutoPlayback) {
      if (isInPictureInPictureMode) {
        viewModel.playNextEpisode()
      } else {
        binding.groupAutoPlayProgress.visibility = View.VISIBLE
        countDownTimer.start()
      }
    }

    if (!viewModel.hasMoreEpisodes()) {
      playbackNotificationManager.setPlayer(null)
    }
  }

  private fun initObservers() {
    viewModel.currentEpisode.observe(viewLifecycleOwner) {
      progressHandler = null
      addToPlaylist(it)
    }

    viewModel.nextEpisode.observe(viewLifecycleOwner) {
      setNextPlaybackItem(it)
    }

    viewModel.bookmarkActionResult.observe(viewLifecycleOwner) {
      when (it?.getContentIfNotHandled()) {
        BookmarkActionDelegate.BookmarkActionResult.BookmarkCreated -> {
          playerBookmarkButton.setIconTintResource(R.color.colorPrimary)
          showSuccessSnackbar(getString(R.string.message_bookmark_created))
        }
        BookmarkActionDelegate.BookmarkActionResult.BookmarkFailedToCreate ->
          showErrorSnackbar(getString(R.string.message_bookmark_failed_to_create))
        BookmarkActionDelegate.BookmarkActionResult.BookmarkDeleted -> {
          playerBookmarkButton.setIconTintResource(R.color.colorIcon)
          showSuccessSnackbar(getString(R.string.message_bookmark_deleted))
        }
        BookmarkActionDelegate.BookmarkActionResult.BookmarkFailedToDelete ->
          showErrorSnackbar(getString(R.string.message_bookmark_failed_to_delete))
        null -> {
          // Houston, We Have a Problem!
        }
      }
    }

    viewModel.serverContentProgress.observe(viewLifecycleOwner) {
      it?.let {
        playerManager.seekTo(it)
      }
    }

    viewModel.resetPlaybackToken.observe(viewLifecycleOwner) {
      it?.let {
        if (it) {
          playerManager.pause()
          showPlaybackTokenErrorBottomSheet()
        }
      }
    }

    viewModel.playerToken.observe(viewLifecycleOwner) {
      if (!it.isNullOrEmpty()) {
        if (isShowingPlaybackTokenBottomSheet()) {
          playbackTokenErrorBottomSheet.dismiss()
        }
        playbackBufferingProgress.toVisibility(false)
        playerManager.resume()
      }
    }

    viewModel.playlist.observe(viewLifecycleOwner) {
      it?.let {
        val showNextButton = viewModel.hasMoreEpisodes() &&
            !viewModel.isContentTypeScreencast()
        playerNextButton.toVisibility(showNextButton)
      }
    }
  }

  private fun showPlaybackTokenErrorBottomSheet() {
    if (::playbackTokenErrorBottomSheet.isInitialized && playbackTokenErrorBottomSheet.isShowing) {
      return
    }
    val sheetView = requireActivity().layoutInflater
      .inflate(R.layout.layout_player_error_playback_token, null)
    playbackTokenErrorBottomSheet = createBottomSheetDialog(sheetView)

    val playButton: View =
      sheetView.findViewById(R.id.button_playback_token_play)
    playButton.setOnClickListener {
      playbackBufferingProgress.toVisibility(true)
      viewModel.resumePlayback()
    }

    playbackTokenErrorBottomSheet.setCancelable(false)
    playbackTokenErrorBottomSheet.show()
  }

  private fun setNextPlaybackItem(nextEpisode: Data?) {
    val visibility =
      if (null != nextEpisode) {
        playerNextButton.text =
          getString(
            R.string.next_episode, nextEpisode.getName()
          )
        View.VISIBLE
      } else {
        View.GONE
      }
    playerNextButton.visibility = visibility
    castNextButton.visibility = visibility
  }

  private fun showPlayerSettingsBottomSheet() {
    if (isShowingPlayerSettingsBottomSheet()) {
      return
    }
    val sheetView = requireActivity().layoutInflater
      .inflate(R.layout.layout_player_settings, null)
    settingsBottomSheet = createBottomSheetDialog(sheetView)
    handlePlaybackQualitySettings(sheetView)
    handlePlaybackSpeedSettings(sheetView)
    handleAutoPlayNextSettings(sheetView)

    settingsBottomSheet.show()
  }

  private fun createBottomSheetDialog(view: View) =
    BottomSheetDialog(requireActivity()).apply {
      setContentView(view)
      setOnShowListener { dialog ->
        val d = dialog as BottomSheetDialog
        val bottomSheet =
          d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as? FrameLayout

        bottomSheet?.let {
          BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }

        val closeButton =
          dialog.findViewById<View>(R.id.button_player_settings_close)
        closeButton?.setOnClickListener { dialog.dismiss() }
      }
    }

  private fun handlePlaybackSpeedSettings(bottomSheetView: View) {
    val playbackSpeedRadioGroup: RadioGroup =
      bottomSheetView.findViewById(R.id.player_playback_speed_radio_group)

    playbackSpeedRadioGroup.setCheckedChildByPosition(
      playerPlaybackSpeedOptions.indexOf(viewModel.getPlaybackSpeed())
    )
    playbackSpeedRadioGroup.setOnCheckedChangeListener { _, checkedId ->
      val checkedPosition = playbackSpeedRadioGroup.getCheckedChildPositionById(checkedId)
      val newSpeed = playerPlaybackSpeedOptions[checkedPosition]
      playerManager.updatePlaybackSpeed(newSpeed)
      viewModel.updatePlaybackSpeed(newSpeed)
    }
  }

  private fun handlePlaybackQualitySettings(bottomSheetView: View) {
    val playbackQualityRadioGroup: RadioGroup =
      bottomSheetView.findViewById(R.id.player_playback_quality_radio_group)

    playbackQualityRadioGroup.setCheckedChildByPosition(
      playerPlaybackQualityOptions.indexOf(viewModel.getPlaybackQuality())
    )
    playbackQualityRadioGroup.setOnCheckedChangeListener { _, checkedId ->
      val checkedPosition = playbackQualityRadioGroup.getCheckedChildPositionById(checkedId)
      val newQuality = playerPlaybackQualityOptions[checkedPosition]
      playerManager.updatePlaybackQuality(newQuality)
      viewModel.updatePlaybackQuality(newQuality)
    }
  }

  private fun handleAutoPlayNextSettings(bottomSheetView: View) {
    val autoPlaybackRadioGroup: RadioGroup =
      bottomSheetView.findViewById(R.id.player_auto_playback_radio_group)

    val autoPlaybackRadioGroupCheckedPosition = if (viewModel.shouldAutoPlay()) {
      0 // R.string.button_label_on
    } else {
      1 // // R.string.button_label_off
    }
    autoPlaybackRadioGroup.setCheckedChildByPosition(
      autoPlaybackRadioGroupCheckedPosition
    )
    autoPlaybackRadioGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
      val checkedPosition = radioGroup.getCheckedChildPositionById(checkedId)
      viewModel.updateAutoPlayNext(checkedPosition != 1)
    }
  }

  private fun isShowingSubtitlesBottomSheet() =
    ::subtitlesBottomSheet.isInitialized && subtitlesBottomSheet.isShowing

  private fun isShowingPlayerSettingsBottomSheet() =
    ::settingsBottomSheet.isInitialized && settingsBottomSheet.isShowing

  private fun isShowingPlaybackTokenBottomSheet() =
    ::playbackTokenErrorBottomSheet.isInitialized && playbackTokenErrorBottomSheet.isShowing


  private fun showSubtitleBottomSheet() {
    if (isShowingSubtitlesBottomSheet()) {
      return
    }
    val sheetView = requireActivity().layoutInflater
      .inflate(R.layout.layout_player_settings_subtitle, null)

    subtitlesBottomSheet = createBottomSheetDialog(sheetView)

    val playbackSubtitleRadioGroup: RadioGroup =
      sheetView.findViewById(R.id.player_auto_playback_radio_group)
    playbackSubtitleRadioGroup.setCheckedChildByPosition(
      playerSubtitleLanguageOptions.indexOf(viewModel.getSubtitleLanguage())
    )
    playbackSubtitleRadioGroup.setOnCheckedChangeListener { _, checkedId ->
      val checkedPosition = playbackSubtitleRadioGroup.getCheckedChildPositionById(checkedId)
      val subtitleLanguage = playerSubtitleLanguageOptions[checkedPosition]
      playerManager.updateSubtitles(subtitleLanguage)
      viewModel.updateSubtitleLanguage(subtitleLanguage)
      subtitlesBottomSheet.dismiss()
    }

    subtitlesBottomSheet.show()
  }

  /**
   * See [Fragment.onDestroy]
   */
  override fun onDestroy() {
    super.onDestroy()
    dismissBottomSheets()
    countDownTimer.cancel()
    requestLandscapeOrientation(false)
    playerManager.release()
    progressHandler?.removeCallbacksAndMessages(null)
    playbackNotificationManager.setPlayer(null)
    pipActionDelegate.clear()
    parentViewModel.updateIsPlaying(false)
  }

  private fun dismissBottomSheets() {
    if (isShowingPlayerSettingsBottomSheet()) {
      settingsBottomSheet.dismiss()
    }
    if (isShowingSubtitlesBottomSheet()) {
      subtitlesBottomSheet.dismiss()
    }
    if (isShowingPlaybackTokenBottomSheet()) {
      playbackTokenErrorBottomSheet.dismiss()
    }
  }

  private fun addToPlaylist(playbackItem: Data?) {
    with(playerManager) {
      addItem(Episode.fromData(playbackItem))
      play(viewModel.shouldAutoPlay())
    }
  }

  private fun updateProgress() {
    if (!binding.playerView.isVisible || !binding.playerView.isAttachedToWindow) {
      return
    }

    viewModel.updateProgress(playerManager.getContentPosition())
  }

  /**
   * See [Fragment.onPictureInPictureModeChanged]
   */
  override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
    super.onPictureInPictureModeChanged(isInPictureInPictureMode)
    this.isInPictureInPictureMode = isInPictureInPictureMode
    if (isInPictureInPictureMode) {
      dismissBottomSheets()
      val receiver = pipActionDelegate.registerReceiver()
      receiver.pipAction.observe(viewLifecycleOwner) {
        when (it) {
          PipActionDelegate.PlaybackAction.PLAY -> {
            playerManager.resume()
            parentViewModel.updateIsPlaying(true)
          }
          PipActionDelegate.PlaybackAction.PAUSE -> {
            playerManager.pause()
            parentViewModel.updateIsPlaying(false)
          }
          PipActionDelegate.PlaybackAction.NEXT -> {
            viewModel.playNextEpisode()
          }
        }
      }
    } else {
      pipActionDelegate.clear()
    }
    with(binding) {
      playerView.useController = !isInPictureInPictureMode
      toolbar.toVisibility(!isInPictureInPictureMode)
    }
  }
}
