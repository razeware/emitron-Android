package com.razeware.emitron.ui.player

import android.annotation.TargetApi
import android.app.Activity
import android.app.PendingIntent
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Parcelable
import android.util.Rational
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.razeware.emitron.R
import com.razeware.emitron.ui.player.PipActionDelegate.PipActionReceiver.Companion.EXTRA_MEDIA_CONTROL_ACTION
import kotlinx.android.parcel.Parcelize

/**
 * Delegate to handle PIP actions
 */
class PipActionDelegate(private val context: Context) {

  /**
   * Playback action
   */
  @Parcelize
  enum class PlaybackAction : Parcelable {
    /**
     * Start/Resume playback
     */
    PLAY,
    /**
     * Pause playback
     */
    PAUSE,
    /**
     * Play next episode
     */
    NEXT
  }

  private val pipActionReceiver = PipActionReceiver()

  /**
   * Register PIP action receiver
   */
  fun registerReceiver(): PipActionReceiver {
    context.registerReceiver(
      pipActionReceiver,
      PipActionReceiver.INTENT_FILTER
    )
    return pipActionReceiver
  }

  /**
   * Clear up registered receiver
   */
  fun clear() {
    try {
      context.unregisterReceiver(pipActionReceiver)
    } catch (e: IllegalArgumentException) {
      // Ignored
    }
  }

  companion object {

    /**
     * Get PIP actions
     */
    @TargetApi(Build.VERSION_CODES.O)
    fun getPipActions(context: Context, isPlaying: Boolean): List<RemoteAction> {

      val createPendingIntent: (Int, PlaybackAction) -> PendingIntent = { requestCode, type ->
        val intent = Intent(PipActionReceiver.ACTION_MEDIA_CONTROL)
          .apply {
            setPackage(context.packageName)
            putExtras(bundleOf(EXTRA_MEDIA_CONTROL_ACTION to type))
          }
        PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
      }


      val nextAction = RemoteAction(
        Icon.createWithResource(context, R.drawable.ic_material_icon_next_episode_2),
        context.getString(R.string.pip_action_next),
        context.getString(R.string.pip_action_next_description),
        createPendingIntent(1, PlaybackAction.NEXT)
      )

      val playPauseAction = if (isPlaying) {
        RemoteAction(
          Icon.createWithResource(context, R.drawable.ic_material_icon_pause),
          context.getString(R.string.pip_action_pause),
          context.getString(R.string.pip_action_pause_description),
          createPendingIntent(2, PlaybackAction.PAUSE)
        )
      } else {
        RemoteAction(
          Icon.createWithResource(context, R.drawable.ic_material_icon_play),
          context.getString(R.string.pip_action_play),
          context.getString(R.string.pip_action_play_description),
          createPendingIntent(2, PlaybackAction.PLAY)
        )
      }
      return listOf(playPauseAction, nextAction)
    }

    /**
     * Get ratio for PIP window
     */
    fun getPipRatio(activity: Activity): Rational {
      val width = activity.window.decorView.width
      val height = activity.window.decorView.height
      return Rational(width, height)
    }
  }

  /**
   * Receiver for PIP actions
   */
  class PipActionReceiver(
    private val _pipAction: MutableLiveData<PlaybackAction> = MutableLiveData(),
    /**
     * Pip action observer
     */
    val pipAction: LiveData<PlaybackAction> = _pipAction
  ) : BroadcastReceiver() {

    companion object {

      /**
       * Intent filter for pip action
       */
      val INTENT_FILTER: IntentFilter = IntentFilter().apply {
        addAction(ACTION_MEDIA_CONTROL)
      }

      /**
       * Action next
       */
      const val ACTION_MEDIA_CONTROL: String = "action_media_control"

      /**
       * Action playback play/pause
       */
      const val EXTRA_MEDIA_CONTROL_ACTION: String = "extra_media_control_action"
    }

    /**
     * See [BroadcastReceiver.onReceive]
     */
    override fun onReceive(context: Context?, intent: Intent?) {
      _pipAction.value = intent?.getParcelableExtra(EXTRA_MEDIA_CONTROL_ACTION)
    }
  }
}
