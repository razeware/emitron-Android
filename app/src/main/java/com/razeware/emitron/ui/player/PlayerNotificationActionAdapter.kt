package com.razeware.emitron.ui.player

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.EXTRA_INSTANCE_ID
import com.razeware.emitron.R

/**
 * Custom action adapter
 */
class PlayerNotificationActionAdapter(
  private val viewModel: PlayerViewModel
) : PlayerNotificationManager.CustomActionReceiver {

  /**
   * See [PlayerNotificationActionAdapter.getCustomActions]
   */
  override fun getCustomActions(player: Player?): List<String> {
    return listOf(ACTION_NEXT)
  }

  /**
   * See [PlayerNotificationActionAdapter.createCustomActions]
   */
  override fun createCustomActions(
    context: Context,
    instanceId: Int
  ): Map<String, NotificationCompat.Action> {

    val intent = Intent(ACTION_NEXT).apply {
      setPackage(context.packageName)
      putExtra(EXTRA_INSTANCE_ID, instanceId)
    }
    val nextAction =
      ACTION_NEXT to
          NotificationCompat.Action.Builder(
            R.drawable.ic_material_icon_next_episode_2, context.getString(
              R.string.pip_action_next
            ), PendingIntent.getBroadcast(
              context, instanceId, intent, PendingIntent.FLAG_UPDATE_CURRENT
            )
          ).build()

    return mapOf(nextAction)
  }

  /**
   * See [PlayerNotificationActionAdapter.onCustomAction]
   */
  override fun onCustomAction(player: Player?, action: String?, intent: Intent?) {
    when (action) {
      ACTION_NEXT -> viewModel.playNextEpisode()
    }
  }

  companion object {
    /**
     * Notification action next
     */
    const val ACTION_NEXT: String = "com.razeware.emitron.next"
  }

}
