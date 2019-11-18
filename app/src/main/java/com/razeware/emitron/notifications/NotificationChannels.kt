package com.razeware.emitron.notifications

import android.annotation.TargetApi
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.annotation.RequiresApi
import com.razeware.emitron.R

class NotificationChannels
/**
 * Constructor
 *
 * @param context The application context
 */
@RequiresApi(api = Build.VERSION_CODES.O)
private constructor(context: Context, private val notificationManager: NotificationManager) :
  ContextWrapper(context) {

  /**
   * Registers notification channels, which can be used later by individual notifications.
   */
  @RequiresApi(api = Build.VERSION_CODES.O)
  fun createNotificationChannels() {
    createLowImpChannel(
      channelIdPlayback,
      getString(R.string.notification_channel_playback)
    )
    createLowImpChannel(channelIdOther, getString(R.string.notification_channel_others))
  }

  /**
   * This method creates a channel of Default Importance
   */
  @RequiresApi(api = Build.VERSION_CODES.O)
  private fun createLowImpChannel(
    channelId: String,
    channelName: String
  ) {
    val notificationChannel =
      NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        .apply {
          enableLights(false)
          setShowBadge(false)
          setBypassDnd(false)
          enableVibration(false)
          lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

    notificationManager.createNotificationChannel(notificationChannel)
  }

  companion object {

    /**
     * Channel id playback
     */
    const val channelIdPlayback: String = "notification.channel.playback"

    /**
     * Channel id others
     */
    const val channelIdOther: String = "notification.channel.others"

    /**
     * Channel id others
     */
    const val channelIdDownloads: String = "notification.channel.downloads"

    /**
     * Create new instance of NotificationChannels
     *
     * @return NotificationChannels
     */
    @TargetApi(Build.VERSION_CODES.O)
    fun newInstance(context: Application): NotificationChannels =
      NotificationChannels(
        context,
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
      )
  }
}
