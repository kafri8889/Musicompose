package com.anafthdev.musicompose.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import com.anafthdev.musicompose.R
import com.anafthdev.musicompose.common.MediaPlayerManager
import com.anafthdev.musicompose.common.broadcast_receiver.MediaPlayerReceiver

class NotificationUtil(context: Context): ContextWrapper(context) {

    private val channelID = "player_notification"
    private val channelName = "Media Player"

    private var notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannel() {
        val channel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
    }

    fun notificationMediaPlayer(mediaStyle: Notification.MediaStyle): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelID)
        } else Notification.Builder(this)

        val playPauseIntent = Intent(this, MediaPlayerReceiver::class.java)
            .setAction(MediaPlayerManager.ACTION_PLAY_PAUSE)
        val playPausePI = PendingIntent.getBroadcast(
            this,
            0,
            playPauseIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val playPauseAction = Notification.Action.Builder(
            Icon.createWithResource(this, R.drawable.ic_play_filled_rounded),
            "PlayPause",
            playPausePI
        ).build()



        val previousIntent = Intent(this, MediaPlayerReceiver::class.java)
            .setAction(MediaPlayerManager.ACTION_PREVIOUS)
        val previousPI = PendingIntent.getBroadcast(
            this,
            0,
            previousIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val previousAction = Notification.Action.Builder(
            Icon.createWithResource(this, R.drawable.ic_previous_filled_rounded),
            "Previous",
            previousPI
        ).build()



        val nextIntent = Intent(this, MediaPlayerReceiver::class.java)
            .setAction(MediaPlayerManager.ACTION_NEXT)
        val nextPI = PendingIntent.getBroadcast(
            this,
            0,
            nextIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val nextAction = Notification.Action.Builder(
            Icon.createWithResource(this, R.drawable.ic_next_filled_rounded),
            "Previous",
            nextPI
        ).build()

        return builder
            .setStyle(mediaStyle)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(previousAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .build()
    }

}