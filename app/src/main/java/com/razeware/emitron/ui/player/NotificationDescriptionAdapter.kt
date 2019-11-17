package com.razeware.emitron.ui.player

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.razeware.emitron.MainActivity

/**
 * Notification description adapter for current playing content
 */
class NotificationDescriptionAdapter(
  private val activity: Activity,
  private val playerViewModel: PlayerViewModel
) : PlayerNotificationManager.MediaDescriptionAdapter {

  /**
   * See [PlayerNotificationManager.MediaDescriptionAdapter.getCurrentContentTitle]
   */
  override fun getCurrentContentTitle(player: Player): String {
    return playerViewModel.getNowPlayingTitle() ?: ""
  }

  /**
   * See [PlayerNotificationManager.MediaDescriptionAdapter.getCurrentContentText]
   */
  override fun getCurrentContentText(player: Player): String? {
    return playerViewModel.getNowPlayingDescription()
  }

  /**
   * See [PlayerNotificationManager.MediaDescriptionAdapter.getCurrentLargeIcon]
   */
  override fun getCurrentLargeIcon(
    player: Player,
    callback: PlayerNotificationManager.BitmapCallback
  ): Bitmap? {
    val coverArt = playerViewModel.getNowPlayingCoverArt()
    Glide.with(activity)
      .asBitmap()
      .load(coverArt)
      .into(object : CustomTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
          callback.onBitmap(resource)
        }

        override fun onLoadCleared(placeholder: Drawable?) {
        }
      })

    return null
  }

  /**
   * See [PlayerNotificationManager.MediaDescriptionAdapter.createCurrentContentIntent]
   */
  override fun createCurrentContentIntent(player: Player): PendingIntent? {
    val intent = Intent(activity, MainActivity::class.java)
      .apply {
        Intent.FLAG_ACTIVITY_SINGLE_TOP or FLAG_ACTIVITY_CLEAR_TOP
      }
    return PendingIntent.getActivity(
      activity, 1,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT,
      null
    )
  }
}
